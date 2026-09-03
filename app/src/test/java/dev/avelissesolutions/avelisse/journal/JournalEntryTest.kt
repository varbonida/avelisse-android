package dev.avelissesolutions.avelisse.journal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the two pure functions behind entry display and search.
 *
 * No Android dependency, so these run without Robolectric.
 */
class JournalEntryTest {

    // --- deriveTitle ---

    @Test
    fun `short text becomes the whole title`() {
        assertEquals("Mauvaise nuit", deriveTitle("Mauvaise nuit"))
    }

    @Test
    fun `title is trimmed of surrounding whitespace`() {
        assertEquals("Mauvaise nuit", deriveTitle("   Mauvaise nuit  \n"))
    }

    @Test
    fun `only the first non-blank line is used`() {
        assertEquals("Mauvaise nuit", deriveTitle("\n\nMauvaise nuit\nPuis douleur au genou"))
    }

    @Test
    fun `empty text gives an empty title`() {
        assertEquals("", deriveTitle("   \n  "))
    }

    @Test
    fun `long text is cut on a word boundary and marked as cut`() {
        val text = "Je me suis reveille cette nuit avec une douleur tres vive dans le genou gauche"
        val title = deriveTitle(text)

        assertTrue("should be marked as cut", title.endsWith("…"))
        assertTrue("should not exceed the limit", title.length <= 61)
        assertTrue("should start the sentence", text.startsWith(title.removeSuffix("…")))
        assertTrue("should not end mid-word", text.startsWith(title.removeSuffix("…") + " "))
    }

    @Test
    fun `a single word longer than the limit is cut hard`() {
        val title = deriveTitle("a".repeat(100))

        assertEquals(61, title.length)
        assertTrue(title.endsWith("…"))
    }

    // --- normalizeForSearch ---

    @Test
    fun `accents are stripped`() {
        assertEquals("fatigue", normalizeForSearch("fatigué"))
        assertEquals("cephalee", normalizeForSearch("céphalée"))
    }

    @Test
    fun `case is folded`() {
        assertEquals("plaquenil", normalizeForSearch("Plaquenil"))
    }

    @Test
    fun `an accented word and its unaccented spelling normalize the same`() {
        assertEquals(normalizeForSearch("Fatigué"), normalizeForSearch("fatigue"))
    }
}
