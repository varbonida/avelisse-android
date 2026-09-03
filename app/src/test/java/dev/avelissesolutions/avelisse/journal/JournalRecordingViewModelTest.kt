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
 * Unit tests for JournalRecordingViewModel.
 *
 * Runs against a real in-memory database so a save is checked by reading the entry back
 * out, not by watching a mock get called.
 *
 * WHY UnconfinedTestDispatcher: the save runs in viewModelScope on Dispatchers.Main.
 * Replacing Main makes it execute on the spot, so the assertion can follow immediately.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@OptIn(ExperimentalCoroutinesApi::class)
class JournalRecordingViewModelTest {

    private lateinit var database: JournalDatabase
    private lateinit var repository: JournalRepository
    private lateinit var viewModel: JournalRecordingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            JournalDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = JournalRepository(database.journalDao())
        viewModel = JournalRecordingViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `a symptom recording is filed under the symptom log`() = runTest {
        viewModel.save(JournalKind.SYMPTOM, "Mauvaise nuit, douleur au genou")

        val entries = repository.observeEntries().first()

        assertEquals(1, entries.size)
        assertEquals(JournalKind.SYMPTOM, entries.first().kind)
        assertEquals("Mauvaise nuit, douleur au genou", entries.first().text)
    }

    @Test
    fun `an appointment recording is filed under the visit log`() = runTest {
        viewModel.save(JournalKind.VISIT, "Le medecin a change le dosage")

        val entries = repository.observeEntries().first()

        assertEquals(1, entries.size)
        assertEquals(JournalKind.VISIT, entries.first().kind)
    }

    @Test
    fun `the two logs stay separate`() = runTest {
        viewModel.save(JournalKind.SYMPTOM, "Douleur au genou")
        viewModel.save(JournalKind.VISIT, "Le medecin a dit")

        assertEquals(1, repository.observeEntries(JournalKind.SYMPTOM).first().size)
        assertEquals(1, repository.observeEntries(JournalKind.VISIT).first().size)
    }

    @Test
    fun `a blank recording files nothing`() = runTest {
        viewModel.save(JournalKind.SYMPTOM, "   ")

        assertTrue(repository.observeEntries().first().isEmpty())
    }
}
