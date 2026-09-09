package dev.avelissesolutions.avelisse.core.theme

import androidx.compose.ui.unit.dp

/**
 * How big a control has to be, and how far it has to sit from its neighbours.
 *
 * These are not general Android numbers. Material's floor is 48dp, which assumes a
 * steady hand: someone who can put a fingertip where they intended. This app is used by
 * people with tremor, weakness and fatigue, often lying down and one-handed, whose taps
 * scatter well past that. Every size here is deliberately above the platform minimum.
 *
 * Use [Minimum] for anything tappable and [Primary] for the actions that must not be
 * missed or mistaken. Prefer `heightIn(min = ...)` over `height(...)`, so a row grows
 * when the system font is enlarged instead of cutting its own text off - the people who
 * enlarge the font are exactly the people this app is for.
 */
object AvelisseTouch {

    /** Smallest a tappable control may be. Material's floor is 48dp; this clears it. */
    val Minimum = 56.dp

    /**
     * Primary and destructive actions: starting a recording, confirming a deletion.
     *
     * Large enough to hit without aiming, because the cost of missing these is either a
     * lost thought or a lost entry.
     */
    val Primary = 64.dp

    /**
     * Least space between two controls that sit next to each other.
     *
     * A tap that lands between two buttons should do nothing. Adjacent controls with no
     * gap turn an unsteady hand into a wrong answer, and one of those wrong answers
     * deletes an entry for good.
     */
    val Separation = 12.dp
}
