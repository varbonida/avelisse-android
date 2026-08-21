package dev.avelissesolutions.avelisse.ui.models

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.avelissesolutions.avelisse.R
import dev.avelissesolutions.avelisse.core.theme.AvelisseColors
import dev.avelissesolutions.avelisse.model.AiProvider
import dev.avelissesolutions.avelisse.model.ModelInfo
import kotlin.math.roundToInt

/**
 * Reusable model card for ModelsScreen and the onboarding model download step.
 *
 * Displays model info (name, provider badge, description, precision/speed bars) and
 * adapts to the download state:
 * - Not downloaded: shows "Télécharger" accent button
 * - Downloading: shows linear progress bar + percentage label
 * - Downloaded: swipe left to reveal red delete button (iOS-style)
 *
 * WHY swipe-to-delete: Matches iOS Avelisse behavior — cards stay compact without a
 * visible delete button, and the swipe gesture is familiar to both iOS and Android users.
 *
 * WHY side-by-side description + bars layout: Matches iOS model card segmented style
 * per CONTEXT.md locked decision. Description on left, precision/vitesse bars on right.
 *
 * WHY "WK" provider badge: Identifies the WhisperKit/whisper.cpp backend, matching
 * iOS visual design for model provenance.
 *
 * @param model          ModelInfo descriptor from the catalog.
 * @param isDownloaded   True if the model file exists on disk with correct size.
 * @param isActive       True if this is the currently selected model.
 * @param downloadProgress Active download progress 0–100, or null if not downloading.
 * @param canDelete      True if this model can be deleted (not the last one).
 * @param hasDownloadError True if the last download attempt failed.
 * @param onDownload     Callback to start downloading this model.
 * @param onDelete       Callback to request deletion (shows confirmation sheet).
 * @param onRetry        Callback to retry after a download failure.
 * @param onSelect       Callback when a downloaded, non-active card is tapped to select it.
 */
@Composable
fun ModelCard(
    model: ModelInfo,
    isDownloaded: Boolean,
    isActive: Boolean,
    downloadProgress: Int?,
    canDelete: Boolean,
    hasDownloadError: Boolean = false,
    isExtracting: Boolean = false,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit,
    onSelect: () -> Unit = {},
) {
    // Press animation state
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "card_scale",
    )

    val isDownloading = downloadProgress != null

    // Active cards get an accent border; others get the default glass border
    val borderColor = if (isActive) AvelisseColors.HomeAccent else AvelisseColors.HomeSurfaceBorder

    // State-driven accent: the active/selected card is teal-branded (matches models.png),
    // non-active cards use neutral dark/gray tones. Reused by the badge, bars, and text below.
    val accentColor = if (isActive) AvelisseColors.HomeAccent else AvelisseColors.ModelsTextPrimary
    val accentColorMuted = if (isActive) AvelisseColors.HomeAccentDark else AvelisseColors.ModelsTextSecondary

    // Swipe-to-delete state
    val deleteButtonWidth = 80.dp
    val density = LocalDensity.current
    val deleteButtonWidthPx = with(density) { deleteButtonWidth.toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "swipe_offset",
    )

    // Card height for the delete button
    var cardHeightPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        // Red delete button behind the card (revealed on swipe left)
        if (isDownloaded && canDelete && animatedOffsetX < -1f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(deleteButtonWidth)
                    .height(with(density) { cardHeightPx.toDp() })
                    .clip(RoundedCornerShape(16.dp))
                    .background(AvelisseColors.Destructive)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            onDelete()
                            offsetX = 0f
                        })
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.model_delete_cd),
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        // Main card content (slides left on swipe)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .onSizeChanged { cardHeightPx = it.height.toFloat() }
                .clip(RoundedCornerShape(16.dp))
                .background(if (isActive) AvelisseColors.HomeAccent.copy(alpha = 0.18f) else AvelisseColors.ModelsSurface)
                .border(
                    if (isActive) 2.dp else 1.dp,
                    borderColor,
                    RoundedCornerShape(16.dp),
                )
                .pointerInput(isDownloaded, isActive, canDelete) {
                    if (isDownloaded && canDelete) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                // Snap: if dragged past half the button width, reveal fully
                                offsetX = if (offsetX < -deleteButtonWidthPx / 2) {
                                    -deleteButtonWidthPx
                                } else {
                                    0f
                                }
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                offsetX = (offsetX + dragAmount)
                                    .coerceIn(-deleteButtonWidthPx, 0f)
                            },
                        )
                    }
                }
                .pointerInput(isDownloaded, isActive) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = {
                            // Reset swipe if open
                            if (offsetX < 0f) {
                                offsetX = 0f
                            } else if (isDownloaded && !isActive) {
                                onSelect()
                            }
                        },
                    )
                }
                .padding(20.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Model header row: name + "WK" provider badge + active chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Model name with inline "WK" provider badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = model.displayName,
                                color = AvelisseColors.ModelsTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            // Provider badge — follows the card's active-state accent (teal when
                            // active/selected, neutral dark otherwise), matching models.png.
                            val (badgeText, badgeColor) = when (model.provider) {
                                AiProvider.WHISPER -> "WK" to accentColor
                                AiProvider.PARAKEET -> "NV" to accentColor
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                )
                            }
                        }
                        if (isActive) {
                            // Active chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AvelisseColors.HomeAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.model_active),
                                    color = AvelisseColors.HomeAccentHighlight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }

                // Persistent amber warning for English-only models
                if (model.isEnglishOnly) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.model_parakeet_language_warning),
                            color = Color(0xFFF59E0B),
                            fontSize = 12.sp,
                        )
                    }
                }

                // Description below name (matches iOS vertical layout)
                if (model.descriptionRes != 0) {
                    Text(
                        text = stringResource(model.descriptionRes),
                        color = accentColorMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }

                // Precision and Vitesse side by side on the same row (matches iOS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SegmentedMetricBar(
                        label = stringResource(R.string.model_precision),
                        value = model.precision,
                        color = accentColor,
                        emptyColor = if (isActive) null else AvelisseColors.HomeSurfaceBorder,
                        modifier = Modifier.weight(1f),
                    )
                    SegmentedMetricBar(
                        label = stringResource(R.string.model_speed),
                        value = model.speed,
                        color = if (isActive) AvelisseColors.HomeAccentHighlight else accentColor,
                        emptyColor = if (isActive) null else AvelisseColors.HomeSurfaceBorder,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Size label
                val sizeMb = model.expectedSizeBytes / 1_000_000
                Text(
                    text = stringResource(R.string.model_size_mb, sizeMb),
                    color = accentColorMuted,
                    fontSize = 12.sp,
                )

                // Download progress or action buttons
                when {
                    isExtracting -> {
                        // Indeterminate progress bar during archive extraction
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AvelisseColors.HomeAccent,
                            trackColor = AvelisseColors.ModelsBackground,
                            strokeCap = StrokeCap.Round,
                        )
                        Text(
                            text = stringResource(R.string.model_extracting),
                            color = AvelisseColors.HomeAccentHighlight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                    isDownloading -> {
                        // Progress bar + percentage
                        val percent = downloadProgress ?: 0
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AvelisseColors.HomeAccent,
                            trackColor = AvelisseColors.ModelsBackground,
                            strokeCap = StrokeCap.Round,
                        )
                        Text(
                            text = stringResource(R.string.model_download_progress, percent),
                            color = AvelisseColors.HomeAccentHighlight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                    hasDownloadError -> {
                        // Error state with retry
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.model_download_error),
                                color = AvelisseColors.Destructive,
                                fontSize = 13.sp,
                            )
                            TextButton(onClick = onRetry) {
                                Text(
                                    text = stringResource(R.string.model_retry),
                                    color = AvelisseColors.Destructive,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                    isDownloaded -> {
                        // No delete button — swipe left to reveal delete action
                    }
                    else -> {
                        // Download button (accent gradient)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            AvelisseColors.HomeAccent,
                                            AvelisseColors.HomeAccentDark,
                                        ),
                                    )
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = { onDownload() })
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.model_download),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * iOS-style segmented metric bar with 5 sub-bars.
 *
 * Matches iOS BrandWaveform model card design: each metric shows a label
 * above a row of 5 rounded segments. Filled segments use the caller-provided
 * accent color, empty segments use a muted background color.
 *
 * @param label Label shown above the segments (e.g. "Précision").
 * @param value Progress value from 0.0 to 1.0 (mapped to 0–5 filled segments).
 * @param emptyColor Optional explicit color for unfilled segments. When null, defaults to
 *        [color] at 15% opacity (e.g. models.png's active/teal card, where the empty segments
 *        are a faded tint of the same hue). Available (non-active) cards pass an explicit tan
 *        tone instead, since their filled segments are neutral gray but empty segments aren't
 *        just a faded gray — they match the card's own surface tone.
 * @param modifier Modifier for the outer Column.
 */
@Composable
private fun SegmentedMetricBar(
    label: String,
    value: Float,
    color: Color = AvelisseColors.HomeAccent,
    emptyColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    val segmentCount = 5
    // Map 0.0–1.0 to 0–5 filled segments (round to nearest)
    val filledCount = (value.coerceIn(0f, 1f) * segmentCount).roundToInt()
    val filledColor = color
    val resolvedEmptyColor = emptyColor ?: color.copy(alpha = 0.15f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            for (i in 0 until segmentCount) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (i < filledCount) filledColor else resolvedEmptyColor),
                )
            }
        }
    }
}
