package dev.avelissesolutions.avelisse.journal

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The on-device database holding every journal entry.
 *
 * There is one table and no remote counterpart. Nothing here is synced, backed up to a
 * server, or readable by another app — it lives in the app's private storage.
 *
 * WHY exportSchema = true: the first migration cannot be written without a record of
 * what version 1 looked like, and that record can only be captured before version 2
 * ships. The generated JSON lives in app/schemas and is committed.
 */
@Database(
    entities = [JournalEntry::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(JournalConverters::class)
abstract class JournalDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao

    companion object {
        /** Renaming this would orphan every entry on an existing install. */
        const val DATABASE_NAME = "avelisse_journal.db"
    }
}
