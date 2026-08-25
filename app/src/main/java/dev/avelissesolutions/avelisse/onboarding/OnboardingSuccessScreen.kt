package dev.avelissesolutions.avelisse.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.avelissesolutions.avelisse.R
import dev.avelissesolutions.avelisse.core.theme.AvelisseColors
import dev.avelissesolutions.avelisse.ui.onboarding.OnboardingStepScaffold
import dev.avelissesolutions.avelisse.ui.onboarding.successGradient

/**
 * Onboarding Step 6 — Success / completion screen.
 *
 * Shown after the model has been downloaded. The user sees a check circle in the
 * Home palette's accent-secondary color, a "C'est pret !" heading, and a brief
 * confirmation. Tapping "Commencer" calls the ViewModel's advanceStep(), which writes
 * HAS_COMPLETED_ONBOARDING=true to DataStore — causing AppNavHost to switch to the
 * main tab layout.
 *
 * WHY success gradient on CTA: The distinct gradient on step 6 reinforces the positive
 * completion state visually. It also signals that this step is different from the
 * previous steps (transition from "in progress" to "done").
 *
 * @param onComplete Called when the user taps "Commencer".
 */
@Composable
fun OnboardingSuccessScreen(
    onComplete: () -> Unit,
) {
    OnboardingStepScaffold(
        currentStep = 7,
        ctaText = stringResource(R.string.onboarding_success_cta),
        ctaGradient = successGradient,
        onCtaClick = onComplete,
    ) {
        // Check circle: 120dp outer frame, full circle, subtle green fill + green border
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(AvelisseColors.Secondary.copy(alpha = 0.2f))
                .border(3.dp, AvelisseColors.Secondary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = AvelisseColors.Secondary,
                modifier = Modifier.size(56.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.onboarding_success_title),
            color = AvelisseColors.TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.onboarding_success_body),
            color = AvelisseColors.TextSecondary,
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
        )
    }
}
