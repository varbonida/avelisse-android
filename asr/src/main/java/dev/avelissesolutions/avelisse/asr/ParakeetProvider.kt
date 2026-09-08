package dev.avelissesolutions.avelisse.asr

import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineNemoEncDecCtcModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig
import dev.avelissesolutions.avelisse.core.stt.SttProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.concurrent.Executors

/**
 * SttProvider implementation backed by sherpa-onnx OfflineRecognizer for
 * NeMo Parakeet models (both CTC and transducer architectures).
 *
 * WHY sherpa-onnx: It ships ONNX Runtime as a pre-built Android JNI library,
 * so we don't need to build ONNX Runtime ourselves. The OfflineRecognizer API
 * wraps the JNI bridge, and NeMo models are supported out of the box.
 *
 * Supports two architectures:
 * - CTC (110M): single model file, English-only
 * - Transducer/TDT (0.6B v3): encoder/decoder/joiner, 25 languages with auto-detection
 *
 * WHY null AssetManager: Models live in the app's files directory, not in assets.
 * Passing null to OfflineRecognizer routes to the newFromFile() JNI path.
 */
class ParakeetProvider : SttProvider {

    override val providerId: String = "parakeet"
    override val displayName: String = "Parakeet (sherpa-onnx)"

    // Parakeet supports both English-only (CTC) and multilingual (TDT v3).
    // Empty list = all languages supported; actual constraint is per-model.
    override val supportedLanguages: List<String> = emptyList()

    private var recognizer: OfflineRecognizer? = null

    /**
     * All sherpa-onnx work runs here, never on the caller's thread.
     *
     * WHY: transcribe() used to run ONNX inference on whichever dispatcher called it,
     * and the caller is the recording screen's composition scope, which is the main
     * thread. A 35-second segment froze the UI for around twelve seconds - long past
     * the point Android offers to kill the app - and no partial text could be drawn
     * because nothing could draw at all. WhisperContext already does exactly this.
     *
     * Single-threaded rather than a shared pool so calls into one recognizer stay
     * serialised, matching the whisper side.
     */
    private val inferenceScope: CoroutineScope = CoroutineScope(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )

    override val isReady: Boolean get() = recognizer != null

    /**
     * Initialize the engine with a model file path.
     *
     * Detects architecture from directory contents:
     * - If encoder.*.onnx exists → transducer config (TDT v3)
     * - Otherwise → CTC config (110M)
     *
     * @param modelPath Absolute path to the main ONNX model file.
     *                  tokens.txt is expected in the same directory.
     * @return true if initialization succeeded.
     */
    override suspend fun initialize(modelPath: String): Boolean {
        val modelDir = modelPath.substringBeforeLast("/")
        val tokensPath = "$modelDir/tokens.txt"

        // Detect transducer by checking for encoder file
        val dir = File(modelDir)
        val encoderFile = dir.listFiles()?.firstOrNull { it.name.startsWith("encoder") && it.name.endsWith(".onnx") }
        val isTransducer = encoderFile != null

        val modelConfig = if (isTransducer) {
            val decoderFile = dir.listFiles()?.firstOrNull { it.name.startsWith("decoder") && it.name.endsWith(".onnx") }
            val joinerFile = dir.listFiles()?.firstOrNull { it.name.startsWith("joiner") && it.name.endsWith(".onnx") }
            if (decoderFile == null || joinerFile == null) {
                Timber.e("Transducer model missing decoder or joiner in %s", modelDir)
                return false
            }
            OfflineModelConfig(
                transducer = OfflineTransducerModelConfig(
                    encoder = encoderFile.absolutePath,
                    decoder = decoderFile.absolutePath,
                    joiner = joinerFile.absolutePath,
                ),
                tokens = tokensPath,
                numThreads = 2,
                provider = "cpu",
                modelType = "nemo_transducer",
            )
        } else {
            OfflineModelConfig(
                nemo = OfflineNemoEncDecCtcModelConfig(model = modelPath),
                tokens = tokensPath,
                numThreads = 2,
                provider = "cpu",
            )
        }

        val config = OfflineRecognizerConfig(
            modelConfig = modelConfig,
            decodingMethod = "greedy_search",
        )

        // Loading the model took over three seconds on a low-end phone, and it ran on
        // the caller's thread, which is the UI thread.
        return withContext(inferenceScope.coroutineContext) {
            try {
                recognizer = OfflineRecognizer(assetManager = null, config = config)
                Timber.d("ParakeetProvider initialized (%s) with model: %s",
                    if (isTransducer) "transducer" else "CTC", modelPath)
                true
            } catch (e: Exception) {
                Timber.e(e, "ParakeetProvider: initialization failed for %s", modelPath)
                false
            }
        }
    }

    /**
     * Transcribe 16kHz mono audio samples to text.
     *
     * For CTC models the language param is ignored (English-only).
     * For transducer v3 models, language is auto-detected from audio.
     *
     * @param samples FloatArray of 16kHz mono audio.
     * @param language BCP-47 language code (ignored — Parakeet auto-detects).
     * @return Raw transcribed text.
     */
    override suspend fun transcribe(samples: FloatArray, language: String): String =
        withContext(inferenceScope.coroutineContext) {
            val r = recognizer ?: throw IllegalStateException("ParakeetProvider not initialized — call initialize() first")
            val stream = r.createStream()
            try {
                stream.acceptWaveform(samples, sampleRate = 16000)
                r.decode(stream)
                r.getResult(stream).text
            } finally {
                // Native memory, not garbage collected. One segment leaked one stream,
                // so a long recording leaked one per half minute of audio.
                stream.release()
            }
        }

    /**
     * Release native resources.
     *
     * Frees the native recognizer, then sets it to null so isReady returns false.
     *
     * WHY the explicit release: dropping the reference alone left the model's native
     * memory to a finalizer, which runs whenever the collector gets round to it. That
     * is 131 MB for the 110M model, held on a phone that is switching to another model
     * precisely because memory is tight.
     */
    override suspend fun release() = withContext(inferenceScope.coroutineContext) {
        recognizer?.release()
        recognizer = null
        Timber.d("ParakeetProvider released")
    }
}
