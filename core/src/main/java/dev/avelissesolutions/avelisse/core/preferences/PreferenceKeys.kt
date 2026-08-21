package dev.avelissesolutions.avelisse.core.preferences

import android.content.res.Resources
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * DataStore preference keys for Avelisse settings.
 *
 * DataStore is Android's type-safe replacement for SharedPreferences.
 * Each key is typed (string, int, boolean, etc.) which prevents
 * type-mismatch bugs at compile time. These keys define what
 * settings are persisted across app restarts.
 *
 * Phase 4 additions: onboarding status, haptics, and sound toggles.
 */
object PreferenceKeys {
    // --- Keyboard settings (Phase 1-2) ---
    val KEYBOARD_LAYOUT = stringPreferencesKey("keyboard_layout")

    // --- Model settings (Phase 3) ---
    val ACTIVE_MODEL = stringPreferencesKey("active_model")
    val TRANSCRIPTION_LANGUAGE = stringPreferencesKey("transcription_language")

    // --- Onboarding (Phase 4) ---
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")

    // --- Audio feedback settings (Phase 4 + Phase 5) ---
    val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val SOUND_VOLUME = floatPreferencesKey("sound_volume")
    val RECORD_START_SOUND = stringPreferencesKey("record_start_sound")
    val RECORD_STOP_SOUND = stringPreferencesKey("record_stop_sound")
    val RECORD_CANCEL_SOUND = stringPreferencesKey("record_cancel_sound")

    // --- Appearance settings (Phase 4) ---
    val THEME = stringPreferencesKey("theme")

    // --- Last transcription (Phase 4) ---
    val LAST_TRANSCRIPTION = stringPreferencesKey("last_transcription")

    // --- Keyboard mode (Phase 5) ---
    /** Default keyboard opening mode: "abc" (letters) or "123" (numbers). */
    val KEYBOARD_MODE = stringPreferencesKey("keyboard_mode")

    // --- Suggestions (Phase 8) ---
    /** Whether the built-in suggestion bar is enabled. Users can disable if they
     *  experience performance issues or prefer typing without suggestions. */
    val SUGGESTIONS_ENABLED = booleanPreferencesKey("suggestions_enabled")

    // --- Personal dictionary (Phase 8) ---
    /** Set of words the user has typed at least twice, persisted across restarts. */
    val PERSONAL_DICTIONARY = stringSetPreferencesKey("personal_dictionary")

    /**
     * Transcription target languages the app actually exposes/supports as a choice
     * (matches the Settings language picker's "fr"/"en" options — not whisper.cpp's
     * much larger internal language vocabulary).
     */
    private val SUPPORTED_TRANSCRIPTION_LANGUAGES = setOf("fr", "en")

    /**
     * Default transcription language to use when [TRANSCRIPTION_LANGUAGE] has never
     * been explicitly set: the device's system language if supported, otherwise English.
     *
     * WHY Resources.getSystem() (not Locale.getDefault()): the app's interface language
     * can be overridden independently via AppCompatDelegate.setApplicationLocales(),
     * which also updates Locale.getDefault() on this process. Resources.getSystem()
     * always reflects the true device/system configuration regardless of that override,
     * keeping interface and transcription language decisions independent as required.
     */
    fun defaultTranscriptionLanguage(): String {
        val systemLanguage = Resources.getSystem().configuration.locales[0].language
        return if (systemLanguage in SUPPORTED_TRANSCRIPTION_LANGUAGES) systemLanguage else "en"
    }
}
