package dev.pivisolutions.dictus.onboarding

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pivisolutions.dictus.R
import dev.pivisolutions.dictus.core.theme.DictusColors
import dev.pivisolutions.dictus.home.DictusWaveformLogo
import dev.pivisolutions.dictus.ui.onboarding.OnboardingStepScaffold

/**
 * Onboarding Step 1 — Welcome screen.
 *
 * Displays the Dictus wordmark with the animated brand waveform logo above it
 * and a tagline below.
 *
 * WHY DictusWaveformLogo (not WaveformBars): DictusWaveformLogo is the app's actual
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
        DictusWaveformLogo()

        Spacer(modifier = Modifier.height(32.dp))

        // "Dictus" wordmark
        Text(
            text = stringResource(R.string.onboarding_welcome_wordmark),
            color = DictusColors.HomeTextPrimary,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = (-0.5).sp,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tagline
        Text(
            text = stringResource(R.string.onboarding_welcome_tagline),
            color = DictusColors.HomeTextSecondary,
            fontSize = 17.sp,
        )
    }
}
