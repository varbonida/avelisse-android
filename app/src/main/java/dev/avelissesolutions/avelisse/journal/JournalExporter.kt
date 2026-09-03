package dev.avelissesolutions.avelisse.journal

import android.content.Context
import android.net.Uri
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Turns entries into a plain text file the person saves wherever they want it.
 *
 * WHY plain text: the point of an export is that somebody else can read it - a doctor, a
 * relative, the person themselves in an email. A .txt opens everywhere and prints, with
 * no app to install and no format to go stale.
 *
 * WHY the system save dialog rather than a share sheet: the file lands somewhere the
 * person chose and can find again, usually Downloads. Nothing here uploads anything, and
 * the app never writes outside the location they picked.
 */
object JournalExporter {

    private val FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * The exported document.
     *
     * @param title Heading for the file.
     * @param entries What to include, in the order they should be read.
     * @param label One line describing an entry, typically its date and which log it came
     *              from. Supplied by the caller because it is localized.
     */
    fun buildExportText(
        title: String,
        entries: List<JournalEntry>,
        label: (JournalEntry) -> String,
    ): String {
        val body = entries.joinToString(separator = "\n\n") { entry ->
            label(entry) + "\n" + entry.text
        }
        return title + "\n\n" + body + "\n"
    }

    /**
     * What the save dialog offers as a name.
     *
     * Dated so a second export sits beside the first instead of prompting to replace it.
     */
    fun suggestedFileName(today: LocalDate): String =
        "avelisse-entries-${FILE_DATE.format(today)}.txt"

    /** Writes the document into the file the person picked in the save dialog. */
    fun writeTo(context: Context, destination: Uri, text: String) {
        context.contentResolver.openOutputStream(destination)?.use { stream ->
            stream.write(text.toByteArray())
        }
    }
}
