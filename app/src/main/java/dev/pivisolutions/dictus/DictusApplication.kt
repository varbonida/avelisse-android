package dev.pivisolutions.dictus

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.pivisolutions.dictus.core.logging.TimberSetup
import timber.log.Timber

/**
 * Main application entry point for Dictus.
 *
 * @HiltAndroidApp triggers Hilt code generation, creating a base class
 * that serves as the application-level dependency container. All Hilt
 * components attach to this application lifecycle.
 *
 * WHY no locale bootstrap here: AppCompatDelegate.getApplicationLocales() is left
 * at its default (empty = "follow system") until the user explicitly picks a
 * language in Settings. Android's own resource resolution already falls back to
 * the default (English) `values/` resources when the system language has no
 * matching `values-xx/` — i.e. "system language if supported, else English" is
 * the native behavior here, with no code required. See SettingsViewModel for the
 * explicit-selection path (AppCompatDelegate.setApplicationLocales()).
 */
@HiltAndroidApp
class DictusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TimberSetup.init(BuildConfig.DEBUG, filesDir)
        Timber.d("Dictus application started")
    }
}
