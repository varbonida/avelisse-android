package dev.avelissesolutions.avelisse.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.avelissesolutions.avelisse.journal.JournalDao
import dev.avelissesolutions.avelisse.journal.JournalDatabase
import javax.inject.Singleton

/**
 * Hilt module providing the journal database.
 *
 * WHY singleton: Room holds a connection pool and a set of invalidation trackers that
 * drive the Flow queries. A second instance over the same file would keep its own
 * trackers and miss the other's writes, so the list would silently stop updating.
 */
@Module
@InstallIn(SingletonComponent::class)
object JournalModule {

    @Provides
    @Singleton
    fun provideJournalDatabase(@ApplicationContext context: Context): JournalDatabase =
        Room.databaseBuilder(
            context,
            JournalDatabase::class.java,
            JournalDatabase.DATABASE_NAME,
        ).build()

    @Provides
    fun provideJournalDao(database: JournalDatabase): JournalDao = database.journalDao()
}
