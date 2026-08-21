package dev.avelissesolutions.avelisse.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Theme modes supported by Avelisse.
 *
 * - DARK: Always use the dark color scheme (default, matches iOS original behavior).
 * - LIGHT: Always use the light color scheme.
 * - AUTO: Follow the system dark/light preference (isSystemInDarkTheme).
 */
enum class ThemeMode { DARK, LIGHT, AUTO }

/**
 * Custom Avelisse color tokens that have no direct Material 3 equivalent.
 *
 * These colors vary between dark and light themes and are provided via
 * [LocalAvelisseColors] so that all UI composables respond to theme changes.
 *
 * WHY a separate data class (not more Material slots): Material 3 has a fixed
 * set of semantic color roles. Keyboard-specific colors (KeyBackground, KeyText)
 * and design-specific tokens (TextSecondary, BorderSubtle) don't map cleanly to
 * any Material role. A CompositionLocal keeps custom colors theme-aware without
 * misusing Material slots.
 */
@Immutable
data class AvelisseColorScheme(
    val textSecondary: Color,
    val borderSubtle: Color,
    val keyBackground: Color,
    val keyText: Color,
    val keySpecialBackground: Color,
    val iconBackground: Color,
)

/** Dark variant of custom Avelisse colors. */
private val AvelisseDarkExtraColors = AvelisseColorScheme(
    textSecondary = AvelisseColors.TextSecondary,
    borderSubtle = AvelisseColors.BorderSubtle,
    keyBackground = AvelisseColors.KeyBackground,
    keyText = AvelisseColors.KeyText,
    keySpecialBackground = AvelisseColors.KeySpecialBackground,
    iconBackground = AvelisseColors.IconBackground,
)

/** Light variant of custom Avelisse colors. */
private val AvelisseLightExtraColors = AvelisseColorScheme(
    textSecondary = AvelisseColors.LightTextSecondary,
    borderSubtle = AvelisseColors.LightBorderSubtle,
    keyBackground = AvelisseColors.LightKeyBackground,
    keyText = AvelisseColors.LightOnSurface,
    keySpecialBackground = AvelisseColors.LightKeySpecialBackground,
    iconBackground = AvelisseColors.LightBackground,
)

/**
 * CompositionLocal providing the current [AvelisseColorScheme].
 *
 * Access via `LocalAvelisseColors.current` inside any composable within [AvelisseTheme].
 */
val LocalAvelisseColors = staticCompositionLocalOf { AvelisseDarkExtraColors }

/**
 * Avelisse dark color scheme using Material 3.
 *
 * Maps Avelisse brand colors to Material 3 semantic roles:
 * - primary/onPrimary: accent blue for interactive elements
 * - background/surface: dark navy tones from the iOS design
 * - error: red used for recording state indicator
 * - tertiary: purple for smart/AI mode indicator
 */
private val AvelisseDarkColorScheme = darkColorScheme(
    primary = AvelisseColors.Accent,
    onPrimary = Color.White,
    primaryContainer = AvelisseColors.AccentHighlight,
    background = AvelisseColors.Background,
    surface = AvelisseColors.Surface,
    onBackground = AvelisseColors.OnBackground,
    onSurface = AvelisseColors.OnSurface,
    error = AvelisseColors.Recording,
    tertiary = AvelisseColors.SmartMode,
)

/**
 * Avelisse light color scheme using Material 3.
 *
 * Maps Avelisse light palette tokens to Material 3 semantic roles.
 * Accent blue remains the same to preserve brand identity.
 */
private val AvelisseLightColorScheme = lightColorScheme(
    primary = AvelisseColors.Accent,
    onPrimary = Color.White,
    primaryContainer = AvelisseColors.AccentHighlight,
    background = AvelisseColors.LightBackground,
    surface = AvelisseColors.LightSurface,
    onBackground = AvelisseColors.LightOnBackground,
    onSurface = AvelisseColors.LightOnSurface,
    error = AvelisseColors.Recording,
    tertiary = AvelisseColors.SmartMode,
)

/**
 * Avelisse application theme.
 *
 * Wraps content in Material 3 theming with the appropriate color scheme
 * based on the requested ThemeMode. Also provides [LocalAvelisseColors] so
 * custom Avelisse color tokens respond to theme changes.
 *
 * @param themeMode The desired theme mode. Defaults to DARK for backward compatibility.
 * @param content The composable content to wrap with the theme.
 */
@Composable
fun AvelisseTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.AUTO -> isSystemInDarkTheme()
    }

    val extraColors = if (useDark) AvelisseDarkExtraColors else AvelisseLightExtraColors

    CompositionLocalProvider(LocalAvelisseColors provides extraColors) {
        MaterialTheme(
            colorScheme = if (useDark) AvelisseDarkColorScheme else AvelisseLightColorScheme,
            typography = AvelisseTypography,
            content = content,
        )
    }
}
