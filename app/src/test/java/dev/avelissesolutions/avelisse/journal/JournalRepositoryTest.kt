package dev.avelissesolutions.avelisse.journal

import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Unit tests for JournalRepository against a real in-memory Room database.
 *
 * Uses the real database rather than a fake DAO so the queries themselves are tested —
 * ordering and the LIKE match are written in SQL, and a fake would not exercise either.
 *
 * Entries are saved with explicit timestamps so ordering assertions do not depend on
 * how fast the test runs.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class JournalRepositoryTest {

    private lateinit var database: JournalDatabase
    private lateinit var repository: JournalRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            JournalDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = JournalRepository(database.journalDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    // --- saveEntry ---

    @Test
    fun `saved entry comes back with its text and kind`() = runTest {
        val saved = repository.saveEntry(JournalKind.SYMPTOM, "Mauvaise nuit", createdAt = 1_000L)

        assertEquals("Mauvaise nuit", saved!!.text)
        assertEquals(JournalKind.SYMPTOM, saved.kind)
        assertEquals(1_000L, saved.createdAt)
        assertEquals(saved, repository.findEntry(saved.id))
    }

    @Test
    fun `text is trimmed before it is stored`() = runTest {
        val saved = repository.saveEntry(JournalKind.VISIT, "  Le medecin a dit  \n", 1_000L)

        assertEquals("Le medecin a dit", saved!!.text)
    }

    @Test
    fun `blank text stores nothing`() = runTest {
        assertNull(repository.saveEntry(JournalKind.SYMPTOM, "   \n ", 1_000L))
        assertTrue(repository.observeEntries().first().isEmpty())
    }

    @Test
    fun `each save gets its own entry`() = runTest {
        val first = repository.saveEntry(JournalKind.SYMPTOM, "Mauvaise nuit", 1_000L)!!
        val second = repository.saveEntry(JournalKind.SYMPTOM, "Mauvaise nuit", 2_000L)!!

        assertEquals(2, repository.observeEntries().first().size)
        assertTrue(first.id != second.id)
    }

    // --- observeEntries ---

    @Test
    fun `entries come back newest first`() = runTest {
        repository.saveEntry(JournalKind.SYMPTOM, "Le plus ancien", 1_000L)
        repository.saveEntry(JournalKind.SYMPTOM, "Le plus recent", 3_000L)
        repository.saveEntry(JournalKind.SYMPTOM, "Celui du milieu", 2_000L)

        val texts = repository.observeEntries().first().map { it.text }

        assertEquals(listOf("Le plus recent", "Celui du milieu", "Le plus ancien"), texts)
    }

    @Test
    fun `entries can be limited to one log`() = runTest {
        repository.saveEntry(JournalKind.SYMPTOM, "Douleur au genou", 1_000L)
        repository.saveEntry(JournalKind.VISIT, "Le medecin a dit", 2_000L)

        val visits = repository.observeEntries(JournalKind.VISIT).first()

        assertEquals(1, visits.size)
        assertEquals("Le medecin a dit", visits.first().text)
    }

    // --- search ---

    @Test
    fun `search matches a word inside an entry`() = runTest {
        repository.saveEntry(JournalKind.SYMPTOM, "Douleur au genou gauche", 1_000L)
        repository.saveEntry(JournalKind.SYMPTOM, "Bonne journee", 2_000L)

        val results = repository.search("genou").first()

        assertEquals(1, results.size)
        assertEquals("Douleur au genou gauche", results.first().text)
    }

    @Test
    fun `search ignores accents and case in both directions`() = runTest {
        repository.saveEntry(JournalKind.SYMPTOM, "Tres fatigué ce matin", 1_000L)
        repository.saveEntry(JournalKind.SYMPTOM, "Pris du Plaquenil", 2_000L)

        assertEquals(1, repository.search("fatigue").first().size)
        assertEquals(1, repository.search("FATIGUÉ").first().size)
        assertEquals(1, repository.search("plaquenil").first().size)
    }

    @Test
    fun `search results are newest first`() = runTest {
        repository.saveEntry(JournalKind.SYMPTOM, "Douleur le lundi", 1_000L)
        repository.saveEntry(JournalKind.SYMPTOM, "Douleur le mardi", 2_000L)

        val texts = repository.search("douleur").first().map { it.text }

        assertEquals(listOf("Douleur le mardi", "Douleur le lundi"), texts)
    }

    @Test
    fun `a blank search returns everything`() = runTest {
        repository.saveEntry(JournalKind.SYMPTOM, "Mauvaise nuit", 1_000L)
        repository.saveEntry(JournalKind.VISIT, "Le medecin a dit", 2_000L)

        assertEquals(2, repository.search("   ").first().size)
    }

    @Test
    fun `a search matching nothing returns nothing`() = runTest {
        repository.saveEntry(JournalKind.SYMPTOM, "Mauvaise nuit", 1_000L)

        assertTrue(repository.search("migraine").first().isEmpty())
    }

    // --- deleteEntry ---

    @Test
    fun `a deleted entry is gone from storage`() = runTest {
        val saved = repository.saveEntry(JournalKind.SYMPTOM, "A supprimer", 1_000L)!!

        repository.deleteEntry(saved.id)

        assertNull(repository.findEntry(saved.id))
        assertTrue(repository.observeEntries().first().isEmpty())
    }

    @Test
    fun `deleting one entry leaves the others`() = runTest {
        val doomed = repository.saveEntry(JournalKind.SYMPTOM, "A supprimer", 1_000L)!!
        repository.saveEntry(JournalKind.SYMPTOM, "A garder", 2_000L)

        repository.deleteEntry(doomed.id)

        val remaining = repository.observeEntries().first()
        assertEquals(1, remaining.size)
        assertEquals("A garder", remaining.first().text)
    }
}
