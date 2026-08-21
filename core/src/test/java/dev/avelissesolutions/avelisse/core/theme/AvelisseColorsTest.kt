package dev.avelissesolutions.avelisse.core.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class AvelisseColorsTest {

    @Test
    fun `background color matches iOS token 0A1628`() {
        assertEquals(Color(0xFF0A1628), AvelisseColors.Background)
    }

    @Test
    fun `accent color matches iOS token 3D7EFF`() {
        assertEquals(Color(0xFF3D7EFF), AvelisseColors.Accent)
    }

    @Test
    fun `surface color matches iOS token 161C2C`() {
        assertEquals(Color(0xFF161C2C), AvelisseColors.Surface)
    }

    @Test
    fun `recording color matches iOS token EF4444`() {
        assertEquals(Color(0xFFEF4444), AvelisseColors.Recording)
    }

    @Test
    fun `success color matches iOS token 22C55E`() {
        assertEquals(Color(0xFF22C55E), AvelisseColors.Success)
    }

    @Test
    fun `all brand colors are defined`() {
        val colors = listOf(
            AvelisseColors.Background,
            AvelisseColors.Accent,
            AvelisseColors.AccentHighlight,
            AvelisseColors.Surface,
            AvelisseColors.Recording,
            AvelisseColors.SmartMode,
            AvelisseColors.Success,
            AvelisseColors.OnBackground,
            AvelisseColors.OnSurface,
            AvelisseColors.KeyBackground,
            AvelisseColors.KeyText,
        )
        assertEquals(11, colors.size)
    }
}
