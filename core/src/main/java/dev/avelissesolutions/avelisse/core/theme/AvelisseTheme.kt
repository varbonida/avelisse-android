package dev.avelissesolutions.avelisse.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Theme modes supported by Avelisse.
 *
 * The Mediterranean Coastal palette (see DESIGN.md) is a single, fixed, light
 * palette with no separate dark variant defined. DARK/LIGHT/AUTO is preserved
 * as a user-facing setting (Settings has a theme picker), but all three modes
 * currently render the same colors — this is a placeholder for a future
 * dark-specific palette, not a functional distinction today.
 */
enum class ThemeMode { DARK, LIGHT, AUTO }

/**
 * Custom Avelisse color tokens that have no direct Material 3 equivalent.
 *
 * WHY a separate data class (not more Material slots): Material 3 has a fixed
 * set of semantic color roles. Keyboard-specific colors (keyBackground, keyText)
 * and design-specific tokens (textSecondary, borderSubtle) don't map cleanly to
 * any Material role. A CompositionLocal keeps these available without misusing
 * Material slots.
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

/** The one Avelisse extra-color set, currently shared by every ThemeMode. */
private val AvelisseExtraColors = AvelisseColorScheme(
    textSecondary = AvelisseColors.TextSecondary,
    borderSubtle = AvelisseColors.Border,
    keyBackground = AvelisseColors.Surface,
    keyText = AvelisseColors.TextPrimary,
    keySpecialBackground = AvelisseColors.SurfaceVariant,
    iconBackground = AvelisseColors.PrimaryContainer,
)

/**
 * CompositionLocal providing the current [AvelisseColorScheme].
 *
 * Access via `LocalAvelisseColors.current` inside any composable within [AvelisseTheme].
 */
val LocalAvelisseColors = staticCompositionLocalOf { AvelisseExtraColors }

/**
 * Avelisse color scheme using Material 3, mapped to the Mediterranean Coastal palette.
 *
 * - primary/onPrimary: sea teal for interactive elements
 * - background/surface: whitewash/white neutrals
 * - error: clay red, also used for the Recording state
 * - tertiary: sun amber accent
 */
private val AvelisseMaterialColorScheme = lightColorScheme(
    primary = AvelisseColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AvelisseColors.PrimaryContainer,
    secondary = AvelisseColors.Secondary,
    secondaryContainer = AvelisseColors.SecondaryContainer,
    background = AvelisseColors.Background,
    surface = AvelisseColors.Surface,
    surfaceVariant = AvelisseColors.SurfaceVariant,
    onBackground = AvelisseColors.TextPrimary,
    onSurface = AvelisseColors.TextPrimary,
    error = AvelisseColors.Error,
    tertiary = AvelisseColors.Accent,
)

/**
 * Avelisse application theme.
 *
 * Wraps content in Material 3 theming and provides [LocalAvelisseColors]. All
 * [ThemeMode] values currently resolve to the same Mediterranean Coastal
 * scheme (see [ThemeMode] doc) — the parameter and the underlying user
 * preference are preserved for when a real dark variant is authored.
 *
 * @param themeMode The desired theme mode (functional preference, no visual effect today).
 * @param content The composable content to wrap with the theme.
 */
@Composable
fun AvelisseTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAvelisseColors provides AvelisseExtraColors) {
        MaterialTheme(
            colorScheme = AvelisseMaterialColorScheme,
            typography = AvelisseTypography,
            content = content,
        )
    }
}
