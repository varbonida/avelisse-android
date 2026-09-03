package dev.avelissesolutions.avelisse.journal

import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one way the app reads and writes journal entries.
 *
 * Keeps the normalization rule ([normalizeForSearch]) in a single place: entries are
 * normalized on the way in, search terms on the way through, so the two can never
 * disagree about what a match is.
 */
@Singleton
class JournalRepository @Inject constructor(
    private val dao: JournalDao,
) {

    /** Every entry, newest first, re-emitted on every change. */
    fun observeEntries(): Flow<List<JournalEntry>> = dao.observeAll()

    /** Entries from one of the two logs, newest first. */
    fun observeEntries(kind: JournalKind): Flow<List<JournalEntry>> = dao.observeByKind(kind)

    /** Matching entries, newest first. A blank query means everything, not nothing. */
    fun search(query: String): Flow<List<JournalEntry>> =
        if (query.isBlank()) dao.observeAll() else dao.search(normalizeForSearch(query))

    suspend fun findEntry(id: String): JournalEntry? = dao.findById(id)

    /**
     * Saves what was said and returns the stored entry.
     *
     * Returns null for blank text so that a recording nobody spoke into leaves nothing
     * behind. There is no error to show — there is simply no entry.
     *
     * @param createdAt Defaults to now. Tests pass their own so ordering is deterministic.
     */
    suspend fun saveEntry(
        kind: JournalKind,
        text: String,
        createdAt: Long = System.currentTimeMillis(),
    ): JournalEntry? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null

        val entry = JournalEntry(
            id = UUID.randomUUID().toString(),
            kind = kind,
            createdAt = createdAt,
            text = trimmed,
            searchText = normalizeForSearch(trimmed),
        )
        dao.upsert(entry)
        return entry
    }

    /** Removes an entry for good. Nothing is kept, because nothing was sent anywhere. */
    suspend fun deleteEntry(id: String) = dao.deleteById(id)
}
