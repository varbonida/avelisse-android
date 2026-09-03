package dev.avelissesolutions.avelisse.journal

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.avelissesolutions.avelisse.core.theme.AvelisseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Compose UI tests for the entries screen.
 *
 * Drives EntriesScreenContent directly so the layout is tested without a database or a
 * Hilt graph behind it.
 */
@RunWith(RobolectricTestRunner::class)
class EntriesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val symptom = JournalEntry(
        id = "a",
        kind = JournalKind.SYMPTOM,
        createdAt = 2_000L,
        text = "Douleur au genou gauche depuis hier soir",
        searchText = normalizeForSearch("Douleur au genou gauche depuis hier soir"),
    )

    private val visit = JournalEntry(
        id = "b",
        kind = JournalKind.VISIT,
        createdAt = 1_000L,
        text = "Le medecin a change le dosage",
        searchText = normalizeForSearch("Le medecin a change le dosage"),
    )

    private fun setScreen(
        entries: List<JournalEntry> = listOf(symptom, visit),
        query: String = "",
        onDelete: (String) -> Unit = {},
        onExport: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            AvelisseTheme {
                EntriesScreenContent(
                    entries = entries,
                    query = query,
                    onQueryChange = {},
                    onDelete = onDelete,
                    onExport = onExport,
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun `entries are listed`() {
        setScreen()

        composeTestRule.onNodeWithText(symptom.text).assertIsDisplayed()
        composeTestRule.onNodeWithText(visit.text).assertIsDisplayed()
    }

    @Test
    fun `an empty journal explains how to start one`() {
        setScreen(entries = emptyList())

        composeTestRule
            .onNodeWithText("Nothing here yet. Press one of the two buttons on the home screen and talk.")
            .assertIsDisplayed()
    }

    @Test
    fun `a search that matches nothing says so instead`() {
        setScreen(entries = emptyList(), query = "migraine")

        composeTestRule.onNodeWithText("Nothing matches that.").assertIsDisplayed()
    }

    @Test
    fun `there is nothing to export from an empty journal`() {
        setScreen(entries = emptyList())

        composeTestRule.onNodeWithContentDescription("Export").assertDoesNotExist()
    }

    @Test
    fun `exporting sends what is on screen`() {
        var exported = 0
        setScreen(onExport = { exported++ })

        composeTestRule.onNodeWithContentDescription("Export").performClick()

        assertEquals(1, exported)
    }

    @Test
    fun `deleting is only offered once an entry is opened`() {
        setScreen()

        composeTestRule.onNodeWithText("Delete").assertDoesNotExist()

        composeTestRule.onNodeWithText(symptom.text).performClick()

        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
    }

    @Test
    fun `deleting asks before it removes anything`() {
        var deleted: String? = null
        setScreen(onDelete = { deleted = it })

        composeTestRule.onNodeWithText(symptom.text).performClick()
        composeTestRule.onNodeWithText("Delete").performClick()

        composeTestRule.onNodeWithText("Delete this entry?").assertIsDisplayed()
        assertNull(deleted)
    }

    @Test
    fun `confirming the dialog deletes that entry`() {
        var deleted: String? = null
        setScreen(onDelete = { deleted = it })

        composeTestRule.onNodeWithText(symptom.text).performClick()
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.onNodeWithText("Delete it").performClick()

        assertEquals(symptom.id, deleted)
    }

    @Test
    fun `keeping it deletes nothing`() {
        var deleted: String? = null
        setScreen(onDelete = { deleted = it })

        composeTestRule.onNodeWithText(symptom.text).performClick()
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.onNodeWithText("Keep it").performClick()

        assertNull(deleted)
    }
}
