package dev.avelissesolutions.avelisse.core.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for ThemeMode enum and theme-related color tokens.
 *
 * The Mediterranean Coastal palette (see DESIGN.md) is a single fixed palette —
 * ThemeMode is preserved as a user-facing setting but no longer selects between
 * two different color schemes (see AvelisseTheme.kt).
 *
 * NOTE: AvelisseTheme composable itself cannot be tested in a pure JVM unit
 * test without a Compose test rule. These tests cover the data layer: enum
 * values and color constants.
 */
class AvelisseThemeTest {

    @Test
    fun `ThemeMode DARK enum value exists`() {
        assertEquals(ThemeMode.DARK, ThemeMode.valueOf("DARK"))
    }

    @Test
    fun `ThemeMode LIGHT enum value exists`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.valueOf("LIGHT"))
    }

    @Test
    fun `ThemeMode AUTO enum value exists`() {
        assertEquals(ThemeMode.AUTO, ThemeMode.valueOf("AUTO"))
    }

    @Test
    fun `ThemeMode has exactly 3 values`() {
        assertEquals(3, ThemeMode.entries.size)
    }

    @Test
    fun `background color matches Mediterranean Coastal token F7F7F7`() {
        assertEquals(Color(0xFFF7F7F7), AvelisseColors.Background)
    }

    @Test
    fun `surface color matches Mediterranean Coastal token FFFFFF`() {
        assertEquals(Color(0xFFFFFFFF), AvelisseColors.Surface)
    }
}
