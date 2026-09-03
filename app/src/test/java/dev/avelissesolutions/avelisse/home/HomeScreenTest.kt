package dev.avelissesolutions.avelisse.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.avelissesolutions.avelisse.core.theme.AvelisseTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Compose UI tests for the Home tab.
 *
 * Covers what the redesign is for: both logs are present and each opens its own flow.
 *
 * WHY autoAdvance is off: the waveform logo animates forever, so a test that waits for
 * the clock to go idle would never return.
 */
@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.mainClock.autoAdvance = false
    }

    private fun setHome(
        onOpenSymptomLog: () -> Unit = {},
        onOpenVisitCapture: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            AvelisseTheme {
                HomeScreen(
                    onOpenSymptomLog = onOpenSymptomLog,
                    onOpenVisitCapture = onOpenVisitCapture,
                )
            }
        }
    }

    @Test
    fun `both logs are on the home screen`() {
        setHome()

        composeTestRule.onNodeWithText("Symptom log").assertIsDisplayed()
        composeTestRule.onNodeWithText("Something happened. Press, talk, done.").assertIsDisplayed()
        composeTestRule.onNodeWithText("After an appointment").assertIsDisplayed()
        composeTestRule.onNodeWithText("What was said, while it is fresh.").assertIsDisplayed()
    }

    @Test
    fun `tapping the symptom log opens the symptom log`() {
        var opened = 0
        setHome(onOpenSymptomLog = { opened++ })

        composeTestRule.onNodeWithText("Symptom log").performClick()

        assertEquals(1, opened)
    }

    @Test
    fun `tapping the appointment log opens the appointment log`() {
        var opened = 0
        setHome(onOpenVisitCapture = { opened++ })

        composeTestRule.onNodeWithText("After an appointment").performClick()

        assertEquals(1, opened)
    }
}
