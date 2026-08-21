package dev.avelissesolutions.avelisse.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.avelissesolutions.avelisse.R
import dev.avelissesolutions.avelisse.core.theme.AvelisseColors
import dev.avelissesolutions.avelisse.home.AvelisseWaveformLogo
import dev.avelissesolutions.avelisse.ui.onboarding.OnboardingStepScaffold

/**
 * Onboarding Step 1 — Welcome screen.
 *
 * Displays the Avelisse wordmark with the animated brand waveform logo above it
 * and a tagline below.
 *
 * WHY AvelisseWaveformLogo (not WaveformBars): AvelisseWaveformLogo is the app's actual
 * brand mark (teal/terracotta soundwave, matches the launcher icon and
 * designs/logo_canvas.html) and is also used on the Home screen and the app's
 * startup loading state — reusing it here keeps the brand mark consistent across
 * all three surfaces instead of showing a generic bar visualizer.
 *
 * @param onNext Called when the user taps "Commencer".
 */
@Composable
fun OnboardingWelcomeScreen(
    onNext: () -> Unit,
) {
    OnboardingStepScaffold(
        currentStep = 1,
        ctaText = stringResource(R.string.onboarding_welcome_cta),
        onCtaClick = onNext,
    ) {
        // Animated brand waveform logo (same composable/animation as Home)
        AvelisseWaveformLogo()

        Spacer(modifier = Modifier.height(32.dp))

        // "AVELISSE" wordmark
        Text(
            text = stringResource(R.string.onboarding_welcome_wordmark),
            color = AvelisseColors.HomeTextPrimary,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = (-0.5).sp,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tagline
        Text(
            text = stringResource(R.string.onboarding_welcome_tagline),
            color = AvelisseColors.HomeTextSecondary,
            fontSize = 17.sp,
        )
    }
}
