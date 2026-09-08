package dev.avelissesolutions.avelisse.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rule that decides where one piece of a recording ends and the next begins.
 *
 * Speech is loud, a pause is quiet, and the read loop already measures that for the
 * waveform. So the cut costs nothing to find: wait until the piece is long enough,
 * then end it once the person has actually stopped talking.
 */
class SegmentBoundaryTest {

    private val manager = AudioCaptureManager()

    private val longEnough = AudioCaptureManager.SEGMENT_MIN_SAMPLES + 1
    private val realPause = AudioCaptureManager.QUIET_RUN_SAMPLES
    private val gapBetweenWords = AudioCaptureManager.QUIET_RUN_SAMPLES - 1
    private val stillTalking = 0

    @Test
    fun `a short piece is never cut, however long the pause`() {
        assertFalse(manager.shouldCloseSegment(samplesInSegment = 1_000, quietRunSamples = realPause))
    }

    @Test
    fun `a piece past the minimum is cut at a real pause`() {
        assertTrue(manager.shouldCloseSegment(longEnough, quietRunSamples = realPause))
    }

    @Test
    fun `a piece past the minimum keeps going while someone is still talking`() {
        assertFalse(manager.shouldCloseSegment(longEnough, quietRunSamples = stillTalking))
    }

    @Test
    fun `a gap between words is not long enough to cut on`() {
        // This is what went wrong on the phone: a 40ms gap inside a sentence read as a
        // pause, so segments ended mid-phrase about a second past the minimum.
        assertFalse(manager.shouldCloseSegment(longEnough, quietRunSamples = gapBetweenWords))
    }

    @Test
    fun `an unbroken talker is cut at the maximum anyway`() {
        assertTrue(
            manager.shouldCloseSegment(
                samplesInSegment = AudioCaptureManager.SEGMENT_MAX_SAMPLES,
                quietRunSamples = stillTalking,
            ),
        )
    }

    @Test
    fun `the minimum is shorter than the maximum`() {
        // Otherwise the pause search never gets a chance to run.
        assertTrue(
            AudioCaptureManager.SEGMENT_MIN_SAMPLES < AudioCaptureManager.SEGMENT_MAX_SAMPLES,
        )
    }

    @Test
    fun `a recording nobody spoke into counts as silent`() {
        assertTrue(manager.isSilent(speechSamples = 0))
        assertTrue(manager.isSilent(AudioCaptureManager.MIN_SPEECH_SAMPLES - 1))
    }

    @Test
    fun `a second of speech is enough to be worth transcribing`() {
        // Below this the engine is handed an empty room, and it answers with invented
        // sentences rather than with nothing.
        assertFalse(manager.isSilent(AudioCaptureManager.MIN_SPEECH_SAMPLES))
    }

    @Test
    fun `a pause fits inside the window between the minimum and the maximum`() {
        // A quiet run longer than the window could never complete before the hard cut.
        val window = AudioCaptureManager.SEGMENT_MAX_SAMPLES - AudioCaptureManager.SEGMENT_MIN_SAMPLES
        assertTrue(AudioCaptureManager.QUIET_RUN_SAMPLES < window)
    }
}
