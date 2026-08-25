package dev.avelissesolutions.avelisse.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Brand color tokens for the Avelisse design system.
 *
 * Mediterranean Coastal palette (see DESIGN.md at the repo root, which is the
 * source of truth for these values): sea teal primary, terracotta secondary,
 * sun amber accent, whitewashed neutrals. One fixed palette is used across the
 * whole app (including the keyboard/IME) — there is no separate dark theme.
 */
object AvelisseColors {
    // --- Primary ---
    val Primary = Color(0xFF007C92)
    val PrimaryContainer = Color(0xFFD9EBEF)

    /** Darkened shade of [Primary], for the second stop of a two-color gradient (DESIGN.md's "primary -> darkened primary" button treatment). */
    val PrimaryDark = Color(0xFF005D6E)

    // --- Secondary ---
    val Secondary = Color(0xFFD85C27)
    val SecondaryContainer = Color(0xFFF9E7DF)

    /** Darkened shade of [Secondary], for the second stop of a two-color gradient. */
    val SecondaryDark = Color(0xFFA2451D)

    // --- Accent (tertiary) ---
    val Accent = Color(0xFFFFB74D)

    // --- Neutral ---
    val Background = Color(0xFFF7F7F7)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF3E5AB)
    val TextPrimary = Color(0xFF071A1D)
    val TextSecondary = Color(0xFF6E8A8F)
    val Border = Color(0xFFD8E2E3)

    // --- Semantic state colors ---
    val Error = Color(0xFFC0392B)
    val Recording = Color(0xFFC0392B) // Same as Error, matching the previous Recording/Destructive convention
    val Destructive = Recording // Semantic alias for Recording
    val Success = Color(0xFF6B8E4E)
}
