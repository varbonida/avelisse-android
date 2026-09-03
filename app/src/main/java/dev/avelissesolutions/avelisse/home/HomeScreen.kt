package dev.avelissesolutions.avelisse.home

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.avelissesolutions.avelisse.R
import dev.avelissesolutions.avelisse.core.theme.AvelisseColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Smallest height of a log button. Large enough to hit without aiming, and a minimum
 * rather than a fixed size so the button grows instead of clipping its label when the
 * system font is scaled up.
 */
private val LOG_BUTTON_MIN_HEIGHT = 148.dp

/** Gap between the two log buttons. Wide enough that a shaky thumb cannot cross it. */
private val LOG_BUTTON_GAP = 20.dp

/**
 * Home tab — the two logs, and nothing competing with them.
 *
 * The person this is built for may be in bed, holding the phone in one hand, on a bad
 * day. So the two buttons sit at the bottom of the screen where a thumb already rests,
 * not centred where they would look best in a screenshot. Everything above them is
 * glanceable and optional.
 *
 * WHY the keyboard and the model are not here: they belong to the dictation keyboard,
 * which is a setting now rather than the point of the app. The active model moved to
 * the Models tab, one tap away.
 *
 * Past entries are one tap away rather than listed here. The link is outlined instead of
 * filled so it reads as a way through rather than a third thing to decide between.
 *
 * @param onOpenEntries Opens the list of past entries.
 * @param onOpenSymptomLog Opens the symptom log recording flow.
 * @param onOpenVisitCapture Opens the post-appointment recording flow.
 */
@Composable
fun HomeScreen(
    onOpenEntries: () -> Unit,
    onOpenSymptomLog: () -> Unit,
    onOpenVisitCapture: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AvelisseColors.Background),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                // At least a screenful, so the arrangement below has room to push the
                // buttons to the bottom. Taller than a screen when the content needs it,
                // at which point the scroll takes over and nothing is out of reach.
                .heightIn(min = maxHeight)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Two children: identity at the top, the two logs at the bottom where a
            // thumb already rests. On a bad day nobody should have to reach.
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AvelisseWaveformLogo()
                Text(
                    text = "AVELISSE",
                    color = AvelisseColors.Primary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EntriesLink(onClick = onOpenEntries)

                Spacer(modifier = Modifier.height(LOG_BUTTON_GAP))

                LogButton(
                    title = stringResource(R.string.home_symptom_title),
                    subtitle = stringResource(R.string.home_symptom_subtitle),
                    icon = Icons.Filled.MonitorHeart,
                    gradient = listOf(AvelisseColors.Primary, AvelisseColors.PrimaryDark),
                    onClick = onOpenSymptomLog,
                )

                Spacer(modifier = Modifier.height(LOG_BUTTON_GAP))

                LogButton(
                    title = stringResource(R.string.home_visit_title),
                    subtitle = stringResource(R.string.home_visit_subtitle),
                    icon = Icons.Filled.MedicalServices,
                    gradient = listOf(AvelisseColors.SecondaryMid, AvelisseColors.SecondaryDark),
                    onClick = onOpenVisitCapture,
                )
            }
        }
    }
}

/**
 * The way through to everything already said.
 *
 * Outlined rather than filled: it is navigation sitting next to the two actions, and it
 * should not compete with them for the first glance.
 */
@Composable
private fun EntriesLink(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AvelisseColors.Surface)
            .border(1.dp, AvelisseColors.Border, RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_entries),
            color = AvelisseColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AvelisseColors.TextSecondary,
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * One of the two log buttons.
 *
 * The whole block is the target, so there is nothing small to aim at. Title and
 * subtitle are read as one item by TalkBack rather than as two stray lines.
 *
 * WHY white text over two dark gradient stops: white has to clear 4.5:1 against every
 * point of the gradient, not only its darkest end. That is why the terracotta button
 * starts at [AvelisseColors.SecondaryMid] rather than the lighter brand secondary.
 */
@Composable
private fun LogButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = LOG_BUTTON_MIN_HEIGHT)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(gradient))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(44.dp),
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

/**
 * Avelisse waveform logo — teal/terracotta soundwave mark matching the app icon
 * (drawable/ic_launcher_foreground.xml), animated to match
 * designs/logo_canvas.html's traveling triple sine-wave + drifting particle motion.
 *
 * Reused across Home, onboarding's Welcome screen, and the app's startup loading
 * state — animate it once here and every call site gets the same motion for free.
 *
 * WHY time read inside Canvas' draw scope (not a List<> rebuilt in the composable
 * body): reading animated state inside a Canvas draw lambda triggers a redraw only,
 * not a recomposition of this composable or its callers — the same "lightweight,
 * frame-synced" approach WaveformDriver.kt already uses via
 * withInfiniteAnimationFrameNanos, just without a separate driver class since this
 * animation has no external input (energy/audio) to smooth toward, unlike WaveformBars.
 *
 * WHY `time += 1f` per frame (not delta-time scaled): logo_canvas.html advances its
 * `time` counter by exactly 1 per requestAnimationFrame callback, so its perceived
 * speed is tied to display refresh rate. Matching that increment reproduces the
 * reference's motion as closely as possible rather than "fixing" it to be frame-rate
 * independent.
 */
private data class LogoWave(
    val amplitude: Float,
    val frequency: Float,
    val speed: Float,
    val thickness: Float,
    val alpha: Float,
    val color: Color,
)

// Icon-space geometry constants, matching ic_launcher_foreground.xml exactly
// (viewport treated as 108x108, wave spans x:[-28,28] around the center).
private const val LOGO_ICON_HALF_WIDTH = 28f
private const val LOGO_REF_HALF_WIDTH = 58.5f
private val LOGO_SCALE = LOGO_ICON_HALF_WIDTH / LOGO_REF_HALF_WIDTH

// Particle orbit constants, matching logo_canvas.html's drawParticles() exactly
// (min(width*0.17, 220) and the fixed 35px y-amplitude, both scaled by LOGO_SCALE).
private const val LOGO_PARTICLE_COUNT = 16
private const val LOGO_PARTICLE_X_RADIUS = 69.7f
private const val LOGO_PARTICLE_Y_AMPLITUDE = 35f

private fun logoEnvelope(normalized: Float): Float =
    (1f - abs(normalized)).coerceAtLeast(0f).pow(0.65f)

@Composable
fun AvelisseWaveformLogo(
    modifier: Modifier = Modifier,
) {
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameNanos {
                time += 1f
            }
        }
    }

    val waveConfigs = listOf(
        LogoWave(amplitude = 22f, frequency = 0.035f, speed = 0.055f, thickness = 3.2f, alpha = 0.9f, color = AvelisseColors.Primary),
        LogoWave(amplitude = 12f, frequency = 0.055f, speed = -0.04f, thickness = 2.3f, alpha = 0.6f, color = AvelisseColors.Secondary),
        LogoWave(amplitude = 34f, frequency = 0.022f, speed = 0.025f, thickness = 2.3f, alpha = 0.35f, color = AvelisseColors.Accent),
    )
    val particleTeal = AvelisseColors.Primary
    val particleTerracotta = AvelisseColors.Secondary

    Canvas(modifier = modifier.size(width = 130.dp, height = 80.dp)) {
        val s = size.width / 108f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val t = time

        // Particles (drawn first / behind waves, matching drawParticles() before
        // drawSoundWaves()). All 16 are drawn unconditionally every frame, exactly
        // matching the reference — no distance-based filtering.
        for (i in 0 until LOGO_PARTICLE_COUNT) {
            val phase = t * 0.025f + i * 0.9f
            val xRef = sin(phase) * LOGO_PARTICLE_X_RADIUS
            val yRef = cos(phase * 1.4f + i) * LOGO_PARTICLE_Y_AMPLITUDE
            val xIcon = xRef * LOGO_SCALE
            val yIcon = yRef * LOGO_SCALE
            val radius = 1.6f + 0.5f * sin(phase * 2f)
            val color = if (i % 3 == 0) particleTerracotta else particleTeal
            drawCircle(
                color = color,
                radius = radius * s,
                center = Offset(cx + xIcon * s, cy + yIcon * s),
                alpha = 0.35f,
            )
        }

        for (w in waveConfigs) {
            val path = Path()
            var x = -LOGO_ICON_HALF_WIDTH
            var first = true
            while (x <= LOGO_ICON_HALF_WIDTH) {
                val xRef = x / LOGO_SCALE
                val normalized = x / LOGO_ICON_HALF_WIDTH
                val envelope = logoEnvelope(normalized)
                val primary = sin(xRef * w.frequency + t * w.speed)
                val secondary = sin(xRef * w.frequency * 1.8f - t * w.speed * 0.7f)
                val deviationRef = (primary * w.amplitude + secondary * w.amplitude * 0.25f) * envelope
                val deviationIcon = deviationRef * LOGO_SCALE
                val px = cx + x * s
                val py = cy + deviationIcon * s
                if (first) {
                    path.moveTo(px, py)
                    first = false
                } else {
                    path.lineTo(px, py)
                }
                x += 0.7f
            }
            drawPath(
                path = path,
                color = w.color,
                alpha = w.alpha,
                style = Stroke(
                    width = w.thickness * s,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}
