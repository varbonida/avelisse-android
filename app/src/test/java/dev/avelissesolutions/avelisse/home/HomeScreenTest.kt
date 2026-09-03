package dev.avelissesolutions.avelisse.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import dev.avelissesolutions.avelisse.core.preferences.PreferenceKeys
import dev.avelissesolutions.avelisse.core.theme.AvelisseTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Compose UI tests for the Home tab.
 *
 * Covers what the redesign is for: both logs are present and tappable, and the
 * keyboard's last transcription is there only when there is one.
 *
 * WHY autoAdvance is off: the waveform logo animates forever, so a test that waits for
 * the clock to go idle would never return.
 */
@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var dataStore: FakeHomeDataStore

    @Before
    fun setUp() {
        composeTestRule.mainClock.autoAdvance = false
        dataStore = FakeHomeDataStore()
    }

    private fun setHome(
        onOpenSymptomLog: () -> Unit = {},
        onOpenVisitCapture: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            AvelisseTheme {
                HomeScreen(
                    dataStore = dataStore,
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

    @Test
    fun `the last transcription is shown when there is one`() {
        dataStore.set(PreferenceKeys.LAST_TRANSCRIPTION, "Douleur au genou gauche")

        setHome()

        composeTestRule.onNodeWithText("Last transcription").assertIsDisplayed()
        composeTestRule.onNodeWithText("Douleur au genou gauche").assertIsDisplayed()
    }

    @Test
    fun `the last transcription is absent when there is none`() {
        setHome()

        composeTestRule.onNodeWithText("Last transcription").assertDoesNotExist()
    }
}

/** In-memory DataStore so the screen can be composed without touching the file system. */
private class FakeHomeDataStore : DataStore<Preferences> {

    private val state = MutableStateFlow<Preferences>(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences,
    ): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }

    fun set(key: Preferences.Key<String>, value: String) {
        state.value = mutablePreferencesOf().apply { this[key] = value }
    }
}
