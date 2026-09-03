package dev.avelissesolutions.avelisse.journal

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.time.LocalDate

/**
 * Unit tests for JournalExporter.
 *
 * The document is checked character for character, because a formatting mistake in an
 * export is silent: it looks fine here and turns up in somebody's email.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class JournalExporterTest {

    private fun entry(id: String, text: String, createdAt: Long) = JournalEntry(
        id = id,
        kind = JournalKind.SYMPTOM,
        createdAt = createdAt,
        text = text,
        searchText = normalizeForSearch(text),
    )

    /** Stands in for the localized date and kind line. */
    private val label: (JournalEntry) -> String = { "on ${it.createdAt}" }

    // --- buildExportText ---

    @Test
    fun `each entry is written under its own label`() {
        val text = JournalExporter.buildExportText(
            title = "Your entries",
            entries = listOf(
                entry("a", "Douleur au genou", 2_000L),
                entry("b", "Bonne journee", 1_000L),
            ),
            label = label,
        )

        assertEquals(
            "Your entries\n\n" +
                "on 2000\nDouleur au genou\n\n" +
                "on 1000\nBonne journee\n",
            text,
        )
    }

    @Test
    fun `entries keep the order they were given`() {
        val text = JournalExporter.buildExportText(
            title = "Your entries",
            entries = listOf(
                entry("a", "Le plus recent", 3_000L),
                entry("b", "Le plus ancien", 1_000L),
            ),
            label = label,
        )

        assertTrue(text.indexOf("Le plus recent") < text.indexOf("Le plus ancien"))
    }

    @Test
    fun `multi-line entries survive intact`() {
        val text = JournalExporter.buildExportText(
            title = "Your entries",
            entries = listOf(entry("a", "Mauvaise nuit.\nPuis douleur au genou.", 1_000L)),
            label = label,
        )

        assertTrue(text.contains("Mauvaise nuit.\nPuis douleur au genou."))
    }

    @Test
    fun `an empty export is still a valid document`() {
        val text = JournalExporter.buildExportText("Your entries", emptyList(), label)

        assertEquals("Your entries\n\n\n", text)
    }

    // --- suggestedFileName ---

    @Test
    fun `the suggested name carries the date and a txt extension`() {
        assertEquals(
            "avelisse-entries-2026-09-03.txt",
            JournalExporter.suggestedFileName(LocalDate.of(2026, 9, 3)),
        )
    }

    @Test
    fun `single digit months and days are padded so names sort chronologically`() {
        assertEquals(
            "avelisse-entries-2026-01-05.txt",
            JournalExporter.suggestedFileName(LocalDate.of(2026, 1, 5)),
        )
    }

    // --- writeTo ---

    @Test
    fun `the document is written into the file the person picked`() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val destination = Uri.parse("content://test/avelisse-entries.txt")
        val written = ByteArrayOutputStream()
        shadowOf(context.contentResolver).registerOutputStream(destination, written)

        JournalExporter.writeTo(context, destination, "Your entries\n\nhello\n")

        assertEquals("Your entries\n\nhello\n", written.toString())
    }
}
