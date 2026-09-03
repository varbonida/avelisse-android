package dev.avelissesolutions.avelisse.journal

import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Unit tests for EntriesViewModel against a real in-memory database.
 *
 * The search is written in SQL, so a fake repository would prove nothing about it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@OptIn(ExperimentalCoroutinesApi::class)
class EntriesViewModelTest {

    private lateinit var database: JournalDatabase
    private lateinit var repository: JournalRepository
    private lateinit var viewModel: EntriesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            JournalDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = JournalRepository(database.journalDao())
        viewModel = EntriesViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    private suspend fun seed() {
        repository.saveEntry(JournalKind.SYMPTOM, "Douleur au genou gauche", 1_000L)
        repository.saveEntry(JournalKind.VISIT, "Le medecin a change le dosage", 2_000L)
        repository.saveEntry(JournalKind.SYMPTOM, "Tres fatigue ce matin", 3_000L)
    }

    @Test
    fun `everything is listed newest first`() = runTest {
        seed()

        val texts = viewModel.entries.first { it.isNotEmpty() }.map { it.text }

        assertEquals(
            listOf("Tres fatigue ce matin", "Le medecin a change le dosage", "Douleur au genou gauche"),
            texts,
        )
    }

    @Test
    fun `searching narrows the list`() = runTest {
        seed()
        viewModel.entries.first { it.isNotEmpty() }

        viewModel.setQuery("genou")

        val results = viewModel.entries.first { it.size == 1 }
        assertEquals("Douleur au genou gauche", results.first().text)
    }

    @Test
    fun `clearing the search brings everything back`() = runTest {
        seed()
        viewModel.setQuery("genou")
        viewModel.entries.first { it.size == 1 }

        viewModel.setQuery("")

        assertEquals(3, viewModel.entries.first { it.size == 3 }.size)
    }

    @Test
    fun `a search matching nothing gives an empty list`() = runTest {
        seed()
        viewModel.entries.first { it.isNotEmpty() }

        viewModel.setQuery("migraine")

        assertTrue(viewModel.entries.first { it.isEmpty() }.isEmpty())
    }

    @Test
    fun `deleting an entry removes it from the list`() = runTest {
        seed()
        val target = viewModel.entries.first { it.size == 3 }.first { it.text.contains("genou") }

        viewModel.deleteEntry(target.id)

        val remaining = viewModel.entries.first { it.size == 2 }
        assertTrue(remaining.none { it.text.contains("genou") })
    }
}
