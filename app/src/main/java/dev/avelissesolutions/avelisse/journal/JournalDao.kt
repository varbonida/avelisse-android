package dev.avelissesolutions.avelisse.journal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Database access for journal entries.
 *
 * WHY Flow for the reads: the entry list has to update the moment a new entry is saved
 * or an old one is deleted. Room emits a fresh list on every write to the table, so the
 * screens observe rather than reload.
 */
@Dao
interface JournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: JournalEntry)

    /** Newest first — the order the browse screen shows them in. */
    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<JournalEntry>>

    /** Newest first, limited to one of the two logs. */
    @Query("SELECT * FROM journal_entries WHERE kind = :kind ORDER BY createdAt DESC")
    fun observeByKind(kind: JournalKind): Flow<List<JournalEntry>>

    /**
     * Substring match against the normalized text.
     *
     * The caller passes an already-normalized term. Matching raw input here would miss
     * every accented word — see [normalizeForSearch].
     */
    @Query(
        "SELECT * FROM journal_entries WHERE searchText LIKE '%' || :normalizedQuery || '%' " +
            "ORDER BY createdAt DESC",
    )
    fun search(normalizedQuery: String): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun findById(id: String): JournalEntry?

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
