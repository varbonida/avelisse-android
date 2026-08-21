package dev.avelissesolutions.avelisse.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.avelissesolutions.avelisse.R
import dev.avelissesolutions.avelisse.core.preferences.PreferenceKeys
import dev.avelissesolutions.avelisse.core.theme.AvelisseColors
import dev.avelissesolutions.avelisse.core.ui.HomeGlassCard
import dev.avelissesolutions.avelisse.model.ModelCatalog
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Home tab screen showing the AVELISSE logo, active model, and new dictation CTA.
 *
 * Layout matches iOS: centered waveform logo + "AVELISSE" wordmark, active model card,
 * and "Nouvelle dictée" button. Content is vertically centered.
 *
 * @param dataStore Application DataStore for reading active model preference.
 * @param onNewDictation Callback for the "Nouvelle dictée" CTA.
 */
@Composable
fun HomeScreen(
    dataStore: DataStore<Preferences>,
    onNewDictation: () -> Unit,
) {
    val activeModelKey by remember(dataStore) {
        dataStore.data.map { it[PreferenceKeys.ACTIVE_MODEL] ?: ModelCatalog.DEFAULT_KEY }
    }.collectAsState(initial = ModelCatalog.DEFAULT_KEY)

    val lastTranscription by remember(dataStore) {
        dataStore.data.map { it[PreferenceKeys.LAST_TRANSCRIPTION] }
    }.collectAsState(initial = null)

    val activeModel = ModelCatalog.findByKey(activeModelKey)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AvelisseColors.HomeBackground)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Waveform logo (soundwave mark matching the app icon)
        AvelisseWaveformLogo()

        Spacer(modifier = Modifier.height(12.dp))

        // "AVELISSE" wordmark in accent teal
        Text(
            text = "AVELISSE",
            color = AvelisseColors.HomeAccent,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle beneath the wordmark — secondary color/size, centered with it.
        Text(
            text = stringResource(R.string.home_tagline),
            color = AvelisseColors.HomeTextSecondary,
            fontSize = 13.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Active model card
        HomeGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.home_active_model),
                color = AvelisseColors.HomeTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text = activeModel?.displayName ?: activeModelKey,
                        color = AvelisseColors.HomeTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (activeModel != null) {
                        val sizeMb = activeModel.expectedSizeBytes / 1_000_000
                        Text(
                            text = stringResource(R.string.model_size_mb, sizeMb),
                            color = AvelisseColors.HomeTextSecondary,
                            fontSize = 13.sp,
                        )
                    }
                }
                // Check circle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AvelisseColors.HomeAccentSecondary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "\u2713",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Last transcription card (only shown when a transcription exists)
        if (!lastTranscription.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            HomeGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.home_last_transcription),
                        color = AvelisseColors.HomeTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.home_copy_cd),
                        tint = AvelisseColors.HomeTextSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                val clipboard = context.getSystemService(
                                    Context.CLIPBOARD_SERVICE,
                                ) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("AVELISSE", lastTranscription),
                                )
                            },
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastTranscription ?: "",
                    color = AvelisseColors.HomeTextPrimary,
                    fontSize = 16.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nouvelle dictée CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(AvelisseColors.HomeAccent, AvelisseColors.HomeAccentDark),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = onNewDictation,
                modifier = Modifier.fillMaxSize(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_new_dictation),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
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
        LogoWave(amplitude = 22f, frequency = 0.035f, speed = 0.055f, thickness = 3.2f, alpha = 0.9f, color = AvelisseColors.HomeAccent),
        LogoWave(amplitude = 12f, frequency = 0.055f, speed = -0.04f, thickness = 2.3f, alpha = 0.6f, color = AvelisseColors.HomeAccentSecondary),
        LogoWave(amplitude = 34f, frequency = 0.022f, speed = 0.025f, thickness = 2.3f, alpha = 0.35f, color = AvelisseColors.HomeAccentHighlight),
    )
    val particleTeal = AvelisseColors.HomeAccent
    val particleTerracotta = AvelisseColors.HomeAccentSecondary

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
