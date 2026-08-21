package dev.avelissesolutions.avelisse.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Brand color tokens for the Avelisse design system.
 *
 * These values are ported directly from the iOS Avelisse app to ensure
 * visual consistency across platforms. Each color maps to a specific
 * role in the UI (background, accent, recording state, etc.).
 */
object AvelisseColors {
    // --- Core brand colors ---
    val Background = Color(0xFF0A1628)
    val Accent = Color(0xFF3D7EFF)
    val AccentHighlight = Color(0xFF6BA3FF)
    val AccentDark = Color(0xFF2563EB)
    val Surface = Color(0xFF161C2C)

    // --- Semantic state colors ---
    val Recording = Color(0xFFEF4444)
    val Destructive = Color(0xFFEF4444) // Semantic alias for Recording
    val SmartMode = Color(0xFF8B5CF6)
    val Success = Color(0xFF22C55E)
    val SuccessDark = Color(0xFF16A34A)
    val SuccessSubtle = Color(0x3322C55E) // #22C55E at 20% opacity

    // --- Text colors ---
    val TextPrimary = Color(0xFFFAFAF9)
    val TextSecondary = Color(0xFF6B6B70)
    val OnBackground = Color.White
    val OnSurface = Color(0xFFE0E0E0)

    // --- Border / overlay colors ---
    val BorderSubtle = Color(0xFF2A2A2E)
    val GlassBorder = Color(0x26FFFFFF)    // White at ~15% opacity
    val InactiveDot = Color(0x4DFFFFFF)    // White at ~30% opacity

    // --- Component colors ---
    val IconBackground = Color(0xFF1E2A4A)
    val KeyBackground = Color(0xFF1E2538)
    val KeyText = Color.White
    val KeySpecialBackground = Color(0xFF2A3347)

    // --- Light palette (matches iOS AvelisseColors.swift light mode tokens) ---
    val LightBackground = Color(0xFFF2F2F7)       // iOS system background light
    val LightSurface = Color(0xFFFFFFFF)           // iOS secondary background light
    val LightOnBackground = Color(0xFF0A1628)      // Dark text on light background
    val LightOnSurface = Color(0xFF1C1C1E)         // Dark text on light surface
    val LightTextSecondary = Color(0xFF8E8E93)     // iOS secondary label light
    val LightBorderSubtle = Color(0xFFD1D1D6)      // iOS separator light
    val LightKeyBackground = Color(0xFFE8E8ED)     // Light key background
    val LightKeySpecialBackground = Color(0xFFD4D4DA) // Light special key background

    // --- Home palette (fixed brand palette for the Home tab; not dark/light reactive) ---
    val HomeBackground = Color(0xFFF5F0E6)
    val HomeSurface = Color(0xFFEFE3CC)
    val HomeSurfaceBorder = Color(0xFFE1D3B4)
    val HomeAccent = Color(0xFF0F7A8C)
    val HomeAccentHighlight = Color(0xFF35A6B5)
    val HomeAccentDark = Color(0xFF0B5C6B)
    val HomeAccentSecondary = Color(0xFFD9662E)
    val HomeAccentSecondaryDark = Color(0xFFA34D23)
    val HomeAccentSecondarySubtle = Color(0x33D9662E) // HomeAccentSecondary at ~20% opacity
    val HomeTextPrimary = Color(0xFF332B22)
    val HomeTextSecondary = Color(0xFF7A6F62)

    // --- Models palette (fixed brand palette for the Models tab; not dark/light reactive) ---
    val ModelsBackground = Color(0xFFF1F2F4)
    val ModelsSurface = Color(0xFFFAF6EC)
    val ModelsTextPrimary = Color(0xFF16181B)
    val ModelsTextSecondary = Color(0xFF5B6472)

    // --- Settings palette (fixed brand palette for the Settings tab; not dark/light reactive) ---
    // Background/surface/text reuse the Home and Models palettes above; this is the one
    // color genuinely new to Settings — the warm tan chip behind picker-row values.
    val SettingsChip = Color(0xFFE8CE93)
}
