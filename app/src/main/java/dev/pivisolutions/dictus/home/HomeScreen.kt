package dev.pivisolutions.dictus.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import dev.pivisolutions.dictus.R
import dev.pivisolutions.dictus.core.preferences.PreferenceKeys
import dev.pivisolutions.dictus.core.theme.DictusColors
import dev.pivisolutions.dictus.core.ui.HomeGlassCard
import dev.pivisolutions.dictus.model.ModelCatalog
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

/**
 * Home tab screen showing the Dictus logo, active model, and new dictation CTA.
 *
 * Layout matches iOS: centered waveform logo + "Dictus" wordmark, active model card,
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
            .background(DictusColors.HomeBackground)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Waveform logo (soundwave mark matching the app icon)
        DictusWaveformLogo()

        Spacer(modifier = Modifier.height(12.dp))

        // "Dictus" wordmark in accent teal
        Text(
            text = "Dictus",
            color = DictusColors.HomeAccent,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Active model card
        HomeGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.home_active_model),
                color = DictusColors.HomeTextSecondary,
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
                        color = DictusColors.HomeTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (activeModel != null) {
                        val sizeMb = activeModel.expectedSizeBytes / 1_000_000
                        Text(
                            text = "~$sizeMb Mo",
                            color = DictusColors.HomeTextSecondary,
                            fontSize = 13.sp,
                        )
                    }
                }
                // Check circle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(DictusColors.HomeAccentSecondary),
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
                        color = DictusColors.HomeTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.home_copy_cd),
                        tint = DictusColors.HomeTextSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                val clipboard = context.getSystemService(
                                    Context.CLIPBOARD_SERVICE,
                                ) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Dictus", lastTranscription),
                                )
                            },
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastTranscription ?: "",
                    color = DictusColors.HomeTextPrimary,
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
                        colors = listOf(DictusColors.HomeAccent, DictusColors.HomeAccentDark),
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
 * Dictus waveform logo — teal/terracotta soundwave mark matching the app icon
 * (drawable/ic_launcher_foreground.xml), which itself is a frozen frame of
 * designs/logo_canvas.html's animated triple sine-wave + particle composition.
 *
 * Reuses the exact same geometry (isotropic scale from the reference's coordinate
 * space) and colors as the launcher icon, computed at draw time rather than as a
 * hardcoded path, and scaled to whatever size this composable is given.
 */
private data class LogoWave(
    val amplitude: Float,
    val frequency: Float,
    val thickness: Float,
    val alpha: Float,
    val color: Color,
)

private data class LogoParticle(
    val dx: Float,
    val dy: Float,
    val radius: Float,
    val color: Color,
)

// Icon-space geometry constants, matching ic_launcher_foreground.xml exactly
// (viewport treated as 108x108, wave spans x:[-28,28] around the center).
private const val LOGO_ICON_HALF_WIDTH = 28f
private const val LOGO_REF_HALF_WIDTH = 58.5f
private val LOGO_SCALE = LOGO_ICON_HALF_WIDTH / LOGO_REF_HALF_WIDTH

private fun logoEnvelope(normalized: Float): Float =
    (1f - abs(normalized)).coerceAtLeast(0f).pow(0.65f)

@Composable
private fun DictusWaveformLogo(
    modifier: Modifier = Modifier,
) {
    val waveConfigs = listOf(
        LogoWave(amplitude = 22f, frequency = 0.035f, thickness = 3.2f, alpha = 0.9f, color = DictusColors.HomeAccent),
        LogoWave(amplitude = 12f, frequency = 0.055f, thickness = 2.3f, alpha = 0.6f, color = DictusColors.HomeAccentSecondary),
        LogoWave(amplitude = 34f, frequency = 0.022f, thickness = 2.3f, alpha = 0.35f, color = DictusColors.HomeAccentHighlight),
    )
    val particles = listOf(
        LogoParticle(dx = -1.60f, dy = 16.75f, radius = 1.60f, color = DictusColors.HomeAccentSecondary),
        LogoParticle(dx = 24.04f, dy = -10.65f, radius = 2.09f, color = DictusColors.HomeAccent),
        LogoParticle(dx = 13.05f, dy = 14.73f, radius = 1.21f, color = DictusColors.HomeAccentSecondary),
        LogoParticle(dx = -16.76f, dy = -15.53f, radius = 2.00f, color = DictusColors.HomeAccent),
        LogoParticle(dx = -26.89f, dy = 9.14f, radius = 1.11f, color = DictusColors.HomeAccentSecondary),
        LogoParticle(dx = -1.06f, dy = -16.65f, radius = 1.62f, color = DictusColors.HomeAccent),
        LogoParticle(dx = 24.40f, dy = 12.03f, radius = 2.08f, color = DictusColors.HomeAccent),
        LogoParticle(dx = 12.53f, dy = -13.74f, radius = 1.22f, color = DictusColors.HomeAccent),
        LogoParticle(dx = -17.27f, dy = 16.13f, radius = 2.01f, color = DictusColors.HomeAccent),
        LogoParticle(dx = -26.53f, dy = -7.51f, radius = 1.11f, color = DictusColors.HomeAccent),
        LogoParticle(dx = -0.51f, dy = 16.33f, radius = 1.63f, color = DictusColors.HomeAccent),
        LogoParticle(dx = 24.73f, dy = -13.26f, radius = 2.08f, color = DictusColors.HomeAccentSecondary),
    )

    Canvas(modifier = modifier.size(width = 130.dp, height = 80.dp)) {
        val s = size.width / 108f
        val cx = size.width / 2f
        val cy = size.height / 2f

        for (p in particles) {
            drawCircle(
                color = p.color,
                radius = p.radius * s,
                center = Offset(cx + p.dx * s, cy + p.dy * s),
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
                val primary = sin(xRef * w.frequency)
                val secondary = sin(xRef * w.frequency * 1.8f)
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
