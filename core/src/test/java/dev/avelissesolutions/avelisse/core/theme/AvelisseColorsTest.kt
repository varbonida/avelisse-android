package dev.avelissesolutions.avelisse.core.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class AvelisseColorsTest {

    @Test
    fun `background color matches Mediterranean Coastal token F7F7F7`() {
        assertEquals(Color(0xFFF7F7F7), AvelisseColors.Background)
    }

    @Test
    fun `primary color matches Mediterranean Coastal token 007C92`() {
        assertEquals(Color(0xFF007C92), AvelisseColors.Primary)
    }

    @Test
    fun `accent color matches Mediterranean Coastal token FFB74D`() {
        assertEquals(Color(0xFFFFB74D), AvelisseColors.Accent)
    }

    @Test
    fun `surface color matches Mediterranean Coastal token FFFFFF`() {
        assertEquals(Color(0xFFFFFFFF), AvelisseColors.Surface)
    }

    @Test
    fun `recording color matches Mediterranean Coastal token C0392B`() {
        assertEquals(Color(0xFFC0392B), AvelisseColors.Recording)
    }

    @Test
    fun `success color matches Mediterranean Coastal token 6B8E4E`() {
        assertEquals(Color(0xFF6B8E4E), AvelisseColors.Success)
    }

    @Test
    fun `all brand colors are defined`() {
        val colors = listOf(
            AvelisseColors.Primary,
            AvelisseColors.PrimaryContainer,
            AvelisseColors.PrimaryDark,
            AvelisseColors.Secondary,
            AvelisseColors.SecondaryContainer,
            AvelisseColors.SecondaryDark,
            AvelisseColors.Accent,
            AvelisseColors.Background,
            AvelisseColors.Surface,
            AvelisseColors.SurfaceVariant,
            AvelisseColors.TextPrimary,
            AvelisseColors.TextSecondary,
            AvelisseColors.Border,
            AvelisseColors.Error,
            AvelisseColors.Recording,
            AvelisseColors.Success,
        )
        assertEquals(16, colors.size)
    }
}
