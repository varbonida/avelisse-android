package dev.avelissesolutions.avelisse.journal

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.avelissesolutions.avelisse.core.theme.AvelisseTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * What the entry list tells a screen reader.
 *
 * These assert the things a person using TalkBack depends on and a sighted person never
 * sees: that a row says which log it belongs to rather than only colouring a dot, that
 * it says whether it is open, and that the two ways out of a delete are both named
 * buttons rather than anonymous text.
 */
@RunWith(RobolectricTestRunner::class)
class EntriesAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val symptom = JournalEntry(
        id = "a",
        kind = JournalKind.SYMPTOM,
        createdAt = 2_000L,
        text = "Bad night, knee hurt again",
        searchText = normalizeForSearch("Bad night, knee hurt again"),
    )

    private val visit = JournalEntry(
        id = "b",
        kind = JournalKind.VISIT,
        createdAt = 1_000L,
        text = "The doctor changed the dosage",
        searchText = normalizeForSearch("The doctor changed the dosage"),
    )

    private fun show(entries: List<JournalEntry> = listOf(symptom, visit)) {
        composeTestRule.setContent {
            AvelisseTheme {
                EntriesScreenContent(
                    entries = entries,
                    query = "",
                    onQueryChange = {},
                    onDelete = {},
                    onExport = {},
                    onBack = {},
                )
            }
        }
    }

    private fun hasState(value: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    @Test
    fun `a row says which log it belongs to, not just which colour it is`() {
        show()

        composeTestRule.onNodeWithText("Symptom", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Appointment", substring = true).assertIsDisplayed()
    }

    @Test
    fun `a row says whether it is open`() {
        show()

        // Matched by the state itself: once a row is open its text appears twice, in the
        // row and in the body below it, so matching on the words is ambiguous.
        composeTestRule.onAllNodes(hasState("Closed")).assertCountEquals(2)

        composeTestRule.onNodeWithText(symptom.text, substring = true).performClick()

        composeTestRule.onNode(hasState("Showing the whole entry")).assertExists()
        composeTestRule.onAllNodes(hasState("Closed")).assertCountEquals(1)
    }

    @Test
    fun `a row is announced as a button`() {
        show()

        composeTestRule.onNodeWithText(symptom.text, substring = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    @Test
    fun `the ways out of the screen are named`() {
        show()

        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Export").assertIsDisplayed()
    }

    @Test
    fun `keeping an entry is offered before deleting it`() {
        show()

        composeTestRule.onNodeWithText(symptom.text, substring = true).performClick()
        composeTestRule.onNodeWithText("Delete").performClick()

        // Both are reachable, and the safe one exists rather than being implied by
        // dismissing the dialog some other way.
        composeTestRule.onNodeWithText("Keep it").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete it").assertIsDisplayed()
    }
}
