package dev.avelissesolutions.avelisse.service

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import dev.avelissesolutions.avelisse.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.math.sqrt

/**
 * Wraps the Android AudioRecord API to capture 16kHz mono Float32 audio.
 *
 * Audio is written to a series of segment files as it is captured, each about
 * half a minute long and ending where the person paused. RMS energy is calculated
 * per read chunk and stored in a rolling 30-entry history for waveform visualization.
 *
 * WHY segments on disk and not one buffer in memory: samples used to accumulate in an
 * ArrayList<Float>, which costs about 20 bytes for every 4-byte sample once boxed. At
 * 16kHz that is roughly 19 MB a minute, so a post-visit capture ran the phone out of
 * heap several minutes in and the person lost everything they had said.
 *
 * WHY cut while recording rather than afterwards: the read loop already measures
 * loudness for the waveform, so a pause is free to spot here. Finding one later would
 * mean reading the whole recording back and searching it.
 *
 * WHY Float32 (ENCODING_PCM_FLOAT): whisper.cpp expects float input.
 * Capturing directly in float avoids a PCM_16BIT-to-float conversion step.
 * Supported on all devices since API 23 (our minSdk is 29).
 *
 * WHY 16kHz: Whisper models expect 16kHz mono audio. Capturing at this rate
 * avoids resampling.
 */
class AudioCaptureManager {

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT
        private const val MAX_ENERGY_HISTORY = 30

        /** Shortest a segment may be before a pause is allowed to end it. */
        const val SEGMENT_MIN_SAMPLES = 25 * SAMPLE_RATE

        /**
         * Longest a segment may run without a pause.
         *
         * Someone talking without a break still gets cut here. That may split a word,
         * which costs one rough join. Letting the segment grow instead would put the
         * memory problem back.
         */
        const val SEGMENT_MAX_SAMPLES = 35 * SAMPLE_RATE

        /** Normalized energy at or below which the microphone is treated as quiet. */
        const val QUIET_ENERGY = 0.5f

        /**
         * How long the quiet has to last before it counts as the person pausing.
         *
         * Each check sees one read buffer, which was 40ms on the phone this was
         * measured on. Ordinary speech is full of gaps that short - between words, and
         * inside stop consonants - so checking a single buffer cut mid-sentence almost
         * every time, within a second of the minimum. 300ms is longer than those gaps
         * and shorter than a real pause.
         */
        const val QUIET_RUN_SAMPLES = SAMPLE_RATE * 3 / 10
    }

    private var recorder: AudioRecord? = null
    private var captureJob: Job? = null
    private var writer: DataOutputStream? = null
    private var segmentDir: File? = null
    private var samplesInSegment = 0
    private var quietRunSamples = 0

    // Diagnostic only: how loud the microphone actually is on this device, in this
    // room. QUIET_ENERGY has been guessed twice and been wrong twice, once too
    // permissive and once too strict, so the value wants setting from a measurement
    // rather than from reasoning about the curve. Remove once it is settled.
    private var rmsWindowMin = Float.MAX_VALUE
    private var rmsWindowMax = 0f
    private var rmsWindowSum = 0f
    private var rmsWindowCount = 0
    private var rmsWindowSamples = 0
    private val segments = mutableListOf<File>()

    // Guards onEnergyUpdate against firing after stop()/cancel() has returned.
    // captureJob.cancel() is cooperative -- the read loop can be blocked inside
    // a synchronous AudioRecord.read() call and still deliver one more energy
    // update after cancellation is requested. Without this guard, that straggler
    // update can land after the caller has already moved the state machine past
    // Recording (e.g. into Transcribing), silently reverting it. Both the flip
    // (in stop()/cancel()) and the check-and-invoke (in the read loop) hold
    // [captureLock] so one fully happens-before the other -- no torn read.
    private val captureLock = Any()
    private var isCapturing = false
    private val energyHistory = ArrayDeque<Float>(MAX_ENERGY_HISTORY).apply {
        // Pre-fill with zeros so the waveform renders all 30 bars immediately.
        // Without this, bars appear to "slide in" from the left as the history fills up.
        repeat(MAX_ENERGY_HISTORY) { addLast(0f) }
    }

    /** Callback invoked on each energy update with a normalized 0.0-1.0 value. */
    var onEnergyUpdate: ((Float) -> Unit)? = null

    /**
     * Start capturing audio.
     *
     * Creates an AudioRecord instance and launches a coroutine that continuously
     * reads samples into the in-memory buffer. Energy updates are posted via
     * [onEnergyUpdate].
     *
     * @param scope CoroutineScope tied to the service lifecycle. The read loop
     *              runs on Dispatchers.Default (background thread pool) so it
     *              doesn't block the main thread.
     * @param dir Directory the segment files are written into. Emptied first, so
     *            anything a previous run left behind goes with it.
     */
    fun start(scope: CoroutineScope, dir: File) {
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (minBufferSize <= 0) {
            Timber.e("AudioRecord.getMinBufferSize returned $minBufferSize -- device may not support ENCODING_PCM_FLOAT")
            return
        }

        // Double the minimum buffer to reduce the risk of buffer underruns
        val bufferSize = minBufferSize * 2

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize,
        ).also { rec ->
            if (rec.state != AudioRecord.STATE_INITIALIZED) {
                Timber.e("AudioRecord failed to initialize (state=${rec.state})")
                rec.release()
                recorder = null
                return
            }
            rec.startRecording()
            Timber.d("AudioRecord started: ${SAMPLE_RATE}Hz mono Float32, buffer=$bufferSize")
        }

        dir.mkdirs()
        dir.listFiles()?.forEach { it.delete() }
        segmentDir = dir
        segments.clear()
        openSegment()

        synchronized(captureLock) { isCapturing = true }

        // Read loop on a background thread.
        // Each read produces a chunk of float samples. We accumulate them
        // and compute RMS energy for the waveform display.
        captureJob = scope.launch(Dispatchers.Default) {
            val readBuffer = FloatArray(minBufferSize / 4) // Float = 4 bytes
            while (isActive) {
                val read = recorder?.read(readBuffer, 0, readBuffer.size, AudioRecord.READ_BLOCKING) ?: break
                if (read > 0) {
                    val rms = calculateRmsEnergy(readBuffer, read)
                    val normalized = normalizeEnergy(rms)

                    if (BuildConfig.DEBUG) recordRmsForTuning(rms, read)

                    writeSamples(readBuffer, read)
                    quietRunSamples = if (normalized <= QUIET_ENERGY) quietRunSamples + read else 0
                    if (shouldCloseSegment(samplesInSegment, quietRunSamples)) {
                        openSegment()
                    }

                    addEnergyToHistory(normalized)
                    synchronized(captureLock) {
                        if (isCapturing) onEnergyUpdate?.invoke(normalized)
                    }
                }
            }
        }
    }

    /**
     * Stop capturing and return the recording, in order.
     *
     * @return the segment files, oldest first. Empty if nothing was captured.
     */
    fun stop(): List<File> {
        synchronized(captureLock) { isCapturing = false }
        captureJob?.cancel()
        captureJob = null
        recorder?.stop()
        recorder?.release()
        recorder = null
        closeWriter()
        resetEnergyHistory()

        // A segment holding no samples is one that was opened and never written to,
        // which happens whenever a recording ends right after a cut.
        val result = segments.filter { it.length() > 0 }.toList()
        segments.clear()
        Timber.d("AudioRecord stopped, %d segment(s)", result.size)
        return result
    }

    /**
     * Cancel capturing and delete the audio.
     */
    fun cancel() {
        stop().forEach { it.delete() }
        segmentDir?.listFiles()?.forEach { it.delete() }
        segmentDir = null
        Timber.d("AudioRecord cancelled, segments deleted")
    }

    /**
     * Whether the segment being written should end here.
     *
     * Ends it once the segment is long enough and the person has been quiet for
     * [QUIET_RUN_SAMPLES], and forces the cut at [SEGMENT_MAX_SAMPLES] so someone who
     * never pauses still gets pieces. Public for testability.
     *
     * @param quietRunSamples How long the microphone has been quiet for, in samples,
     *                        reset to zero the moment anything louder arrives.
     */
    fun shouldCloseSegment(samplesInSegment: Int, quietRunSamples: Int): Boolean = when {
        samplesInSegment >= SEGMENT_MAX_SAMPLES -> true
        samplesInSegment >= SEGMENT_MIN_SAMPLES -> quietRunSamples >= QUIET_RUN_SAMPLES
        else -> false
    }

    /** Close the current segment, if any, and begin the next one. */
    private fun openSegment() {
        closeWriter()
        val dir = segmentDir ?: return
        val file = File(dir, "segment-%03d.pcm".format(segments.size))
        segments += file
        writer = DataOutputStream(BufferedOutputStream(FileOutputStream(file)))
        samplesInSegment = 0
        quietRunSamples = 0
    }

    private fun writeSamples(buffer: FloatArray, count: Int) {
        val out = writer ?: return
        for (i in 0 until count) out.writeFloat(buffer[i])
        samplesInSegment += count
    }

    /**
     * Logs the raw RMS range roughly twice a second while recording.
     *
     * Raw, not the [normalizeEnergy] value: that curve multiplies by 20 and takes a
     * square root so quiet sounds show up on the waveform, which squashes speech and
     * room tone together near the top and makes it useless for telling them apart.
     */
    private fun recordRmsForTuning(rms: Float, read: Int) {
        rmsWindowMin = minOf(rmsWindowMin, rms)
        rmsWindowMax = maxOf(rmsWindowMax, rms)
        rmsWindowSum += rms
        rmsWindowCount++
        rmsWindowSamples += read

        if (rmsWindowSamples < SAMPLE_RATE / 2) return
        Timber.d(
            "RMS over %.1fs: min=%.5f mean=%.5f max=%.5f",
            rmsWindowSamples.toFloat() / SAMPLE_RATE,
            rmsWindowMin,
            rmsWindowSum / rmsWindowCount,
            rmsWindowMax,
        )
        rmsWindowMin = Float.MAX_VALUE
        rmsWindowMax = 0f
        rmsWindowSum = 0f
        rmsWindowCount = 0
        rmsWindowSamples = 0
    }

    private fun closeWriter() {
        runCatching { writer?.close() }
            .onFailure { Timber.e(it, "Failed to close segment writer") }
        writer = null
        samplesInSegment = 0
    }

    /**
     * Calculate the root-mean-square energy of a buffer of audio samples.
     *
     * RMS = sqrt( sum(sample^2) / count )
     *
     * This gives a single energy value representing the "loudness" of the chunk.
     * Public for testability.
     *
     * @param buffer Array of float samples (typically -1.0 to 1.0).
     * @param count Number of valid samples in the buffer (may be less than buffer.size).
     * @return RMS energy value. Returns 0f if count is 0.
     */
    fun calculateRmsEnergy(buffer: FloatArray, count: Int): Float {
        if (count == 0) return 0f
        var sum = 0f
        for (i in 0 until count) {
            sum += buffer[i] * buffer[i]
        }
        return sqrt(sum / count)
    }

    /**
     * Normalize a raw RMS energy value to the 0.0-1.0 range for display.
     *
     * Uses a power curve (sqrt) for better perceptual mapping: quiet speech
     * still produces visible bar movement, while loud speech reaches full height.
     * The 20x multiplier maps typical speech RMS (0.005-0.15) to ~0.3-1.0
     * after the sqrt curve, giving responsive visual feedback.
     *
     * @param rms Raw RMS energy value.
     * @return Normalized value in [0.0, 1.0].
     */
    fun normalizeEnergy(rms: Float): Float {
        val amplified = (rms * 20f).coerceIn(0f, 1f)
        return sqrt(amplified) // sqrt curve: boosts quiet sounds, compresses loud
    }

    /**
     * Add a normalized energy value to the rolling history.
     *
     * The history maintains at most [MAX_ENERGY_HISTORY] (30) entries.
     * When full, the oldest entry is dropped. This provides the data
     * for the 30-bar waveform visualization.
     */
    fun addEnergyToHistory(energy: Float) {
        energyHistory.addLast(energy)
        if (energyHistory.size > MAX_ENERGY_HISTORY) {
            energyHistory.removeFirst()
        }
    }

    /**
     * Get a snapshot of the current energy history for waveform display.
     *
     * @return Immutable list of up to 30 normalized energy values.
     */
    fun getEnergyHistory(): List<Float> = energyHistory.toList()

    /**
     * Reset energy history to 30 zero entries.
     * Ensures the next recording starts with a full-width flat waveform
     * instead of bars sliding in from the left.
     */
    private fun resetEnergyHistory() {
        energyHistory.clear()
        repeat(MAX_ENERGY_HISTORY) { energyHistory.addLast(0f) }
    }
}

/**
 * Reads one segment file back into samples.
 *
 * A segment runs at most 35 seconds, so this is a couple of megabytes at worst. That
 * is the point of segments: transcription never holds more than one at a time, however
 * long the recording was.
 *
 * DataOutputStream.writeFloat is big-endian, which is also ByteBuffer's default, so the
 * two agree without setting an order.
 */
fun readSegment(file: File): FloatArray {
    val bytes = file.readBytes()
    val samples = FloatArray(bytes.size / 4)
    ByteBuffer.wrap(bytes).asFloatBuffer().get(samples)
    return samples
}
