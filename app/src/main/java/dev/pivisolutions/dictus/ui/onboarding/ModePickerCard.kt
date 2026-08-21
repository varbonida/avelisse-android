package dev.pivisolutions.dictus.ui.onboarding

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import dev.pivisolutions.dictus.R
import dev.pivisolutions.dictus.core.theme.DictusColors

/**
 * Two side-by-side keyboard mode selection cards for onboarding step 4.
 *
 * Presents two options:
 * - "ABC" (azerty/abc layout) — starts with letters visible
 * - "123" (numeric layout) — starts with numbers visible
 *
 * The selected card gets a Home accent-teal border (2dp) and teal text.
 * The unselected card gets the Home palette's subtle border (1dp) and muted text.
 *
 * WHY two cards (not radio buttons / Chips): The UI-SPEC calls for large, tappable
 * cards that clearly show what each layout looks like. The visual differentiation
 * (size, emoji-style label, color) is intentional for users unfamiliar with IME settings.
 *
 * @param selectedLayout Layout key — "azerty" for letters, "numeric" for numbers.
 * @param onSelect       Called with the newly selected layout key.
 */
@Composable
fun ModePickerCard(
    selectedLayout: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LayoutOptionCard(
            label = "ABC",
            description = stringResource(R.string.onboarding_mode_letters),
            layoutKey = "azerty",
            isSelected = selectedLayout == "azerty",
            onSelect = onSelect,
            modifier = Modifier.weight(1f),
        )
        LayoutOptionCard(
            label = "123",
            description = stringResource(R.string.onboarding_mode_numbers),
            layoutKey = "numeric",
            isSelected = selectedLayout == "numeric",
            onSelect = onSelect,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Individual layout option card within the mode picker.
 */
@Composable
private fun LayoutOptionCard(
    label: String,
    description: String,
    layoutKey: String,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "card_scale_$layoutKey",
    )

    val borderColor = if (isSelected) DictusColors.HomeAccent else DictusColors.HomeSurfaceBorder
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val labelColor = if (isSelected) DictusColors.HomeAccent else DictusColors.HomeTextSecondary
    val descColor = if (isSelected) DictusColors.HomeTextPrimary else DictusColors.HomeTextSecondary

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(DictusColors.HomeSurface)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onSelect(layoutKey) },
                )
            }
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Large layout label ("ABC" or "123")
        Text(
            text = label,
            color = labelColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        // Description label
        Text(
            text = description,
            color = descColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )

        // Selection indicator: filled check circle (selected) or empty circle outline (unselected)
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = stringResource(R.string.cd_selected),
                tint = DictusColors.HomeAccent,
                modifier = Modifier.size(24.dp),
            )
        } else {
            // Empty circle outline for unselected state
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(2.dp, DictusColors.HomeSurfaceBorder, CircleShape),
            )
        }
    }
}
