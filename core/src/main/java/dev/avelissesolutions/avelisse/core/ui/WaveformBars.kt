package dev.avelissesolutions.avelisse.core.ui

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.avelissesolutions.avelisse.core.theme.AvelisseColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

/**
 * Canvas-based 30-bar waveform visualization for recording overlays.
 *
 * Ported from iOS BrandWaveform. Each bar is a rounded rectangle (pill shape)
 * drawn on a single Canvas for optimal performance (single GPU draw call).
 *
 * Color scheme (matching iOS BrandWaveform.resolvedBarColor), overridable via
 * [innerColor]/[outerColor] for callers on a different palette:
 *   - Center 40% of bars: brand blue by default (AvelisseColors.Accent)
 *   - Outer 60%: white/gray by default (theme-dependent) with opacity decreasing toward edges
 *
 * Bar height formula (matching iOS):
 *   height = max(minHeight + energy * (maxHeight - minHeight), minHeight)
 *   - minHeight = 2dp (idle) or 4dp (processing): always-visible baseline
 *   - This ensures bars scale energy within the available height range
 *     rather than raw multiplication which under-utilizes the space
 *
 * Processing mode: when isProcessing=true, bars show a traveling sine wave
 * using WaveformDriver.processingEnergy() instead of live energy levels.
 *
 * This composable lives in core so both the app module and the ime module
 * can reuse it without duplication.
 */
@Composable
fun WaveformBars(
    energyLevels: List<Float>,
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
    processingPhase: Double = 0.0,
    innerColor: Color = AvelisseColors.Accent,
    outerColor: Color? = null,
) {
    val paddedLevels = padEnergy(energyLevels)
    // Outer bar base color: white in dark theme, gray in light theme (matches iOS),
    // unless the caller explicitly overrides it (e.g. for a fixed light-palette
    // screen like Recording, where the ambient dark-theme default would render
    // near-invisible white bars against its ivory background).
    val isDark = MaterialTheme.colorScheme.background == AvelisseColors.Background
    val outerBarBase = outerColor ?: if (isDark) Color.White else Color(0xFF8E8E93)

    Canvas(modifier = modifier) {
        val barCount = WaveformDriver.BAR_COUNT
        val gapPx = 2.dp.toPx()
        val barWidth = (size.width - gapPx * (barCount - 1)) / barCount
        // Min bar height: 2dp idle (thin visible baseline), 4dp processing
        // Matches iOS: let minHeight: CGFloat = driver.isProcessing ? 4 : 2
        val minBarHeight = if (isProcessing) 4.dp.toPx() else 2.dp.toPx()
        val maxBarHeight = size.height
        val centerY = size.height / 2f

        for (index in 0 until barCount) {
            val energy: Float = if (isProcessing) {
                WaveformDriver.processingEnergy(index, processingPhase, barCount)
            } else {
                paddedLevels.getOrElse(index) { 0f }
            }

            // iOS formula: max(minHeight + energy * (maxHeight - minHeight), minHeight)
            // This maps energy 0..1 to minBarHeight..maxBarHeight linearly.
            val barHeight = maxOf(minBarHeight + energy * (maxBarHeight - minBarHeight), minBarHeight)
            val x = index * (barWidth + gapPx)
            val color = barColor(index, barCount, outerBarBase, innerColor)

            drawRoundRect(
                color = color,
                topLeft = Offset(x, centerY - barHeight / 2f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f),
            )
        }
    }
}

/**
 * Pads or trims the energy list to exactly 30 entries.
 *
 * - Fewer than 30 items: right-padded with 0f
 * - More than 30 items: last 30 items are taken
 * - Exactly 30 items: returned as-is
 */
internal fun padEnergy(levels: List<Float>): List<Float> {
    return when {
        levels.size >= 30 -> levels.takeLast(30)
        else -> levels + List(30 - levels.size) { 0f }
    }
}

/**
 * Determines the color for a waveform bar at the given index.
 *
 * Matches iOS BrandWaveform.resolvedBarColor:
 *   - Distance from center < 0.4 (inner 40%): brand blue (Accent)
 *   - Distance from center >= 0.4 (outer 60%): white with opacity
 *     = (1.0 - distanceFromCenter) * 0.9 + 0.15
 *
 * WHY distance-based instead of fixed indices: The iOS version uses
 * a continuous distance metric which scales correctly if bar count changes.
 * This also produces smoother opacity gradients at the transition boundary.
 */
/**
 * @param outerBase Base color for outer bars: White in dark theme, gray in light theme.
 *   Matches iOS where outer bars are gray (#8E8E93) on light backgrounds.
 * @param innerColor Solid color for the inner 40% of bars. Defaults to the original
 *   brand blue; callers on a different palette (e.g. the Home-palette Recording
 *   screen) can override it without affecting other callers.
 */
internal fun barColor(index: Int, barCount: Int, outerBase: Color = Color.White, innerColor: Color = AvelisseColors.Accent): Color {
    val center = (barCount - 1) / 2f
    val distanceFromCenter = abs(index - center) / center

    // Inner 40%: solid brand color
    if (distanceFromCenter < 0.4f) {
        return innerColor
    }

    // Outer 60%: outerBase with opacity decreasing toward edges
    // Matches iOS: Double(1.0 - distanceFromCenter) * 0.9 + 0.15
    val opacity = (1.0f - distanceFromCenter) * 0.9f + 0.15f
    return outerBase.copy(alpha = opacity)
}

/**
 * Filled, mirrored, organically-textured waveform matching
 * designs/waveform_canvas.html — shape, movement and rhythm are ported
 * directly from that reference, while overall amplitude reacts to real-time
 * microphone input volume.
 *
 * Ported directly from that reference's drawWaveform(time)/animate(timestamp):
 * - Shape per horizontal position x (0..1): a fixed "landscape" of 5 Gaussian
 *   peaks at specific x-positions, an edge envelope tapering to 0 at both ends,
 *   and 3 traveling sine waves (different spatial frequencies/speeds/directions)
 *   giving continuous organic micro-texture — all exactly as in the reference.
 * - `time` advances by real elapsed seconds (via withInfiniteAnimationFrameNanos'
 *   frame timestamp), matching the reference's `timestamp*0.001*speed` — i.e.
 *   animation speed is tied to actual clock time, not frame count.
 *
 * WHY smoothing lives here (not a separate driver class): [volume] is fed a raw,
 * already-normalized (0.0-1.0) mic energy sample every time new audio arrives —
 * see [dev.avelissesolutions.avelisse.core.service.DictationState.Recording.energy].
 * That raw value can change abruptly between samples, so it's eased toward with
 * a fast-rise/slow-decay formula (same constants as [WaveformDriver.tickLevels])
 * once per animation frame, in the same loop that already advances [time]. This
 * keeps the whole animation self-contained in one composable with no extra
 * classes/StateFlows — [rememberUpdatedState] is what lets that single
 * long-running frame loop always read the latest [volume] passed in by the
 * caller, since a plain captured parameter would otherwise go stale.
 *
 * Start/stop/reset is handled by the caller only composing this while it should
 * be visible/animating (e.g. inside an `if (isRecording) { WaveformBlob(...) }`
 * block) — leaving composition cancels the internal LaunchedEffect, and the next
 * time it's composed it's a fresh instance starting from time = 0, volume = 0.
 *
 * @param volume Raw mic input level, 0.0 (silence) to 1.0 (loudest), sampled
 *   fresh each time new audio arrives. Internally smoothed and floored so
 *   near-silence settles to a calm, minimal shape rather than flattening away.
 * @param color Fill color.
 * @param edgeColor Soft highlight stroked around the shape's edge (reference's
 *   `edgeColor` — a lighter shade used for shadow/stroke).
 */
@Composable
fun WaveformBlob(
    volume: Float = WAVEFORM_BLOB_VOLUME,
    modifier: Modifier = Modifier,
    color: Color = AvelisseColors.Accent,
    edgeColor: Color = AvelisseColors.Accent,
) {
    val currentVolume by rememberUpdatedState(volume)
    var time by remember { mutableFloatStateOf(0f) }
    var smoothedVolume by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameNanos { frameTimeNanos ->
                time = (frameTimeNanos * 1e-9 * WAVEFORM_BLOB_SPEED).toFloat()

                // Fast-rise/slow-decay easing (same shape as WaveformDriver.tickLevels)
                // so louder input grows the waveform quickly while quiet moments
                // settle back down smoothly instead of jumping or flickering.
                val target = currentVolume.coerceIn(0f, 1f)
                smoothedVolume = if (target > smoothedVolume) {
                    smoothedVolume + (target - smoothedVolume) * WAVEFORM_BLOB_RISE
                } else {
                    target + (smoothedVolume - target) * WAVEFORM_BLOB_DECAY
                }
            }
        }
    }

    // Floor so near-silence settles to a calm, minimal shape instead of fully flattening.
    val effectiveVolume = smoothedVolume.coerceAtLeast(WAVEFORM_BLOB_MIN_VOLUME)

    Canvas(modifier = modifier) {
        val centerY = size.height / 2f
        val maxHeight = size.height * 0.36f
        val amplitude = maxHeight * effectiveVolume
        val resolutionPx = 3f
        val pointCount = (size.width / resolutionPx).toInt().coerceAtLeast(2)

        val topYs = FloatArray(pointCount + 1)
        for (i in 0..pointCount) {
            val x = i.toFloat() / pointCount

            // Edge envelope: tapers the shape to 0 at x=0 and x=1.
            // coerceAtLeast(0f) guards against float32 precision noise: PI.toFloat()
            // rounds slightly ABOVE true pi, so sin(PI.toFloat() * 1f) is a tiny
            // NEGATIVE value instead of exactly/near 0 — and Float.pow() of a
            // negative base to a fractional exponent (0.7) returns NaN, which then
            // poisons every coordinate in the Path and makes the whole shape invisible.
            val envelope = sin(PI.toFloat() * x).coerceAtLeast(0f).pow(0.7f)

            // Three traveling sine waves at different spatial frequencies and
            // speeds/directions — continuous organic micro-texture.
            val wave1 = sin(x * 11f + time * 1.25f)
            val wave2 = sin(x * 23f - time * 0.75f)
            val wave3 = sin(x * 43f + time * 0.45f)
            val shape = abs(wave1 * 0.42f + wave2 * 0.20f + wave3 * 0.08f)

            // Fixed landscape of 5 Gaussian peaks at specific x-positions —
            // gives the shape recognizable, non-uniform structure across its width.
            val peaks = 0.22f +
                gaussianPeak(x, 0.13f, 0.055f) * 0.38f +
                gaussianPeak(x, 0.25f, 0.045f) * 0.95f +
                gaussianPeak(x, 0.49f, 0.06f) * 0.60f +
                gaussianPeak(x, 0.61f, 0.055f) * 1.00f +
                gaussianPeak(x, 0.81f, 0.075f) * 0.50f

            topYs[i] = amplitude * envelope * peaks * (0.65f + shape)
        }

        val path = Path()
        path.moveTo(0f, centerY - topYs[0])
        for (i in 1..pointCount) {
            val x = (i.toFloat() / pointCount) * size.width
            path.lineTo(x, centerY - topYs[i])
        }
        for (i in pointCount downTo 0) {
            val x = (i.toFloat() / pointCount) * size.width
            path.lineTo(x, centerY + topYs[i])
        }
        path.close()

        drawPath(path = path, color = color)
        drawPath(
            path = path,
            color = edgeColor,
            alpha = 0.5f,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

/** Matches settings.speed in designs/waveform_canvas.html. */
private const val WAVEFORM_BLOB_SPEED = 0.9f

/** Default [WaveformBlob.volume] — matches settings.volume in designs/waveform_canvas.html. */
private const val WAVEFORM_BLOB_VOLUME = 0.75f

/** Fast-rise factor for smoothing [WaveformBlob]'s volume — matches WaveformDriver's smoothingFactor. */
private const val WAVEFORM_BLOB_RISE = 0.3f

/** Slow-decay factor for smoothing [WaveformBlob]'s volume — matches WaveformDriver's decayFactor. */
private const val WAVEFORM_BLOB_DECAY = 0.85f

/** Floor so near-silence still renders a calm, minimal [WaveformBlob] shape. */
private const val WAVEFORM_BLOB_MIN_VOLUME = 0.1f

/** Gaussian bump centered at [center] (0..1 x-position) with the given [width]. */
private fun gaussianPeak(x: Float, center: Float, width: Float): Float {
    val z = (x - center) / width
    return exp(-z * z)
}
