package dev.avelissesolutions.avelisse.journal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.text.Normalizer

/** Which of the two logs an entry belongs to. */
enum class JournalKind {
    /** Symptom log — recorded whenever something happens. */
    SYMPTOM,

    /** Post-visit capture — recorded in the minutes after an appointment. */
    VISIT,
}

/**
 * One spoken entry, stored on the device and nowhere else.
 *
 * WHY the transcript only (no audio): the transcript is what gets browsed, searched
 * and exported. Keeping the audio as well would multiply storage for something nothing
 * currently reads. If that changes, the audio path is a new column and a migration.
 *
 * WHY no stored title: the title is the trimmed opening line of [text], so it is a
 * function of the text. Storing it would let the two drift apart. See [deriveTitle].
 *
 * WHY a stored [searchText]: SQLite's LIKE folds case for ASCII only, so a search for
 * "fatigue" would miss "fatigué" — unusable in a French-first app about symptoms. This
 * column holds the accent-stripped lowercase form that queries actually match against.
 * Unlike the title, it cannot be computed at read time because the database is what
 * runs the query.
 *
 * @param id Stable identifier, generated once when the entry is saved.
 * @param kind Which of the two logs this belongs to.
 * @param createdAt Epoch milliseconds at the moment the entry was saved.
 * @param text The transcript, exactly as the person said it.
 * @param searchText [text] normalized for matching. Always set via [normalizeForSearch].
 */
@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey val id: String,
    val kind: JournalKind,
    val createdAt: Long,
    val text: String,
    val searchText: String,
)

/** Longest title we will show before cutting the opening line short. */
private const val MAX_TITLE_LENGTH = 60

/**
 * The trimmed opening line of an entry, used as its title in the list.
 *
 * Not a summary. Summarising would need a language model, and running one off the
 * device would break the rule that nothing leaves the phone.
 *
 * Cuts on a word boundary when the opening line is too long, so a title never ends
 * mid-word. Falls back to a hard cut if the first word alone is longer than the limit.
 */
fun deriveTitle(text: String): String {
    val firstLine = text.trim().lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
    if (firstLine.length <= MAX_TITLE_LENGTH) return firstLine

    val cut = firstLine.take(MAX_TITLE_LENGTH)
    val lastSpace = cut.lastIndexOf(' ')
    val body = if (lastSpace > 0) cut.take(lastSpace) else cut
    return body.trimEnd() + "…"
}

/** Matches Unicode combining marks, which is what an accent becomes after NFD. */
private val COMBINING_MARKS = Regex("""\p{Mn}+""")

/**
 * Lowercases and strips accents so search matches the way a person expects.
 *
 * "Fatigué" and "fatigue" both normalize to "fatigue". Applied to both the stored
 * entry and the search term, so the two always meet in the same form.
 */
fun normalizeForSearch(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .lowercase()

/** Room cannot persist an enum directly, so [JournalKind] crosses as its name. */
class JournalConverters {

    @TypeConverter
    fun kindToName(kind: JournalKind): String = kind.name

    @TypeConverter
    fun nameToKind(name: String): JournalKind = JournalKind.valueOf(name)
}
