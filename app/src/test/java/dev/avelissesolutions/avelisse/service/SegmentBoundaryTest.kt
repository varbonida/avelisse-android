package dev.avelissesolutions.avelisse.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rule that decides where one piece of a recording ends and the next begins.
 *
 * Speech is loud, a pause is quiet, and the read loop already measures that for the
 * waveform. So the cut costs nothing to find: wait until the piece is long enough,
 * then end it the moment the person stops talking.
 */
class SegmentBoundaryTest {

    private val manager = AudioCaptureManager()

    private val quiet = 0.1f
    private val speech = 0.9f

    @Test
    fun `a short piece is never cut, even in silence`() {
        assertFalse(manager.shouldCloseSegment(samplesInSegment = 1_000, energy = quiet))
    }

    @Test
    fun `a piece past the minimum is cut at a pause`() {
        val past = AudioCaptureManager.SEGMENT_MIN_SAMPLES + 1

        assertTrue(manager.shouldCloseSegment(samplesInSegment = past, energy = quiet))
    }

    @Test
    fun `a piece past the minimum keeps going while someone is still talking`() {
        val past = AudioCaptureManager.SEGMENT_MIN_SAMPLES + 1

        assertFalse(manager.shouldCloseSegment(samplesInSegment = past, energy = speech))
    }

    @Test
    fun `an unbroken talker is cut at the maximum anyway`() {
        val atMax = AudioCaptureManager.SEGMENT_MAX_SAMPLES

        assertTrue(manager.shouldCloseSegment(samplesInSegment = atMax, energy = speech))
    }

    @Test
    fun `the minimum is shorter than the maximum`() {
        // Otherwise the pause search never gets a chance to run.
        assertTrue(
            AudioCaptureManager.SEGMENT_MIN_SAMPLES < AudioCaptureManager.SEGMENT_MAX_SAMPLES,
        )
    }
}
