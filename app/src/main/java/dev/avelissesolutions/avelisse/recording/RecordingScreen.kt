package dev.avelissesolutions.avelisse.recording

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.avelissesolutions.avelisse.R
import dev.avelissesolutions.avelisse.core.service.DictationController
import dev.avelissesolutions.avelisse.core.service.DictationState
import androidx.compose.ui.semantics.Role
import dev.avelissesolutions.avelisse.core.theme.AvelisseTouch
import dev.avelissesolutions.avelisse.core.theme.AvelisseColors
import dev.avelissesolutions.avelisse.core.ui.HomeGlassCard
import dev.avelissesolutions.avelisse.core.ui.WaveformBars
import dev.avelissesolutions.avelisse.core.ui.WaveformBlob
import dev.avelissesolutions.avelisse.core.ui.WaveformDriver
import kotlinx.coroutines.launch

/**
 * Standalone recording screen — triggered from Home via "Nouvelle dictée".
 *
 * Reuses the recording/transcription flow from OnboardingTestRecordingScreen but
 * without any onboarding-specific elements (progress dots, Passer/Continuer buttons).
 *
 * Layout uses a layered Box so the mic/stop button stays at a fixed position
 * (200dp from bottom) regardless of state:
 * 1. Upper content (idle text, "Recording..." + timer, or result card) — fills
 *    the area above the button
 * 2. Bottom block (waveform + button) — anchored to BottomCenter
 * 3. Close button — top-left, always visible
 *
 * Recording auto-starts on first composition so the user lands directly in recording
 * state — matching iOS Avelisse behavior where tapping "Nouvelle dictée" starts immediately.
 *
 * The screen records and transcribes; what happens to the transcript is the caller's
 * decision. It is handed over through [onTranscribed] rather than written here, so the
 * screen has no opinion about where an entry is filed.
 *
 * @param dictationController Controller for the DictationService (may be null if not yet bound).
 * @param onTranscribed Called once with the finished transcript.
 * @param onBack Called when the user leaves the screen.
 */
@Composable
fun RecordingScreen(
    dictationController: DictationController?,
    onTranscribed: (String) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val dictationState by dictationController?.state?.collectAsState()
        ?: remember { mutableStateOf(DictationState.Idle as DictationState) }

    var transcriptionResult by remember { mutableStateOf<String?>(null) }
    var copied by remember { mutableStateOf(false) }

    // Auto-start recording on first composition — iOS parity: tapping "Nouvelle dictée"
    // brings the user directly into the recording state without a manual tap.
    LaunchedEffect(Unit) {
        dictationController?.startRecording()
    }

    // Leaving mid-recording has to stop it. There is no route back into this screen to
    // finish it, so a recording left running is one nobody can reach, still holding the
    // microphone and still writing to disk.
    val leave = {
        if (dictationState is DictationState.Recording) dictationController?.cancelRecording()
        onBack()
    }

    // Processing animation driver for the transcribing state.
    // Uses WaveformDriver.processingEnergy() — same formula as iOS BrandWaveformDriver.
    val processingDriver = remember {
        WaveformDriver().apply { isProcessing = true }
    }
    val processingPhase by processingDriver.processingPhase.collectAsState()

    // Run the processing animation loop when transcribing.
    // LaunchedEffect cancels when isTranscribing becomes false.
    if (dictationState is DictationState.Transcribing) {
        LaunchedEffect(Unit) {
            processingDriver.runLoop()
        }
    }

    val hasResult = transcriptionResult != null
    val isRecording = dictationState is DictationState.Recording
    val isTranscribing = dictationState is DictationState.Transcribing

    val noResultLabel = stringResource(R.string.recording_no_result)

    // No horizontal padding on the root Box: the recording waveform bleeds to the
    // full screen width (matches recording.png). Everything else (close button,
    // text, mic/stop button) applies its own 32dp horizontal padding below instead.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AvelisseColors.Background),
    ) {
        // ── Leave button (bottom-left, always visible) ──
        //
        // It used to sit in the top-left corner at about 40dp. That is the hardest
        // place on a large phone to reach with one thumb, it needed an accurate tap,
        // and it is the only way out of this screen - so getting it wrong meant
        // shifting grip, which is the second hand we cannot assume anyone has.
        //
        // Down here it is in reach, but deliberately not beside the stop button in the
        // middle: leaving mid-recording throws the audio away, and that must never be
        // the thing a shaky hand hits while aiming for "I have finished talking".
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 88.dp)
                .size(AvelisseTouch.Minimum)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = leave),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.recording_close_cd),
                tint = AvelisseColors.TextPrimary,
                modifier = Modifier.size(28.dp),
            )
        }

        // ── Layer 1: Upper content (idle text, "Recording..." + timer, or result card) ──
        when {
            hasResult -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 32.dp)
                        .padding(top = 160.dp, bottom = 240.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.recording_saved_title),
                        color = AvelisseColors.TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    HomeGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = transcriptionResult ?: "",
                            color = AvelisseColors.TextPrimary,
                            fontSize = 17.sp,
                            lineHeight = 26.sp,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .align(Alignment.End)
                                .clip(CircleShape)
                                .clickable {
                                    val clipboard = context.getSystemService(
                                        Context.CLIPBOARD_SERVICE,
                                    ) as ClipboardManager
                                    clipboard.setPrimaryClip(
                                        ClipData.newPlainText("AVELISSE", transcriptionResult),
                                    )
                                    copied = true
                                }
                                .padding(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.recording_copy_cd),
                                tint = if (copied) AvelisseColors.Secondary else AvelisseColors.TextSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            isRecording -> {
                // "Recording..." label + timer, centered above the waveform (matches recording.png).
                val recording = dictationState as DictationState.Recording
                RecordingLabelAndTimer(
                    elapsedMs = recording.elapsedMs,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 32.dp)
                        .padding(top = 200.dp),
                )
            }

            !isTranscribing -> {
                // Idle: title + subtitle centered above the button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 32.dp)
                        .padding(top = 200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.recording_idle_title),
                        color = AvelisseColors.TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.recording_idle_body),
                        color = AvelisseColors.TextSecondary,
                        fontSize = 15.sp,
                        lineHeight = (15 * 1.5).sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // ── Layer 2: Bottom block (waveform + button) ──
        // For recording: waveform (teal, matches recording.png) grouped with the button.
        // "Recording..." + timer now live in Layer 1, above the waveform (matches reference layout).
        // For idle: just the mic button.
        // For result: teal mic button to re-record.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!hasResult) {
                // Waveform (only during recording/transcribing)
                when {
                    isRecording -> {
                        val recording = dictationState as DictationState.Recording

                        // Animation style/movement from designs/waveform_canvas.html;
                        // amplitude reacts to real mic input via the existing energy
                        // history already exposed on DictationState.Recording — composed
                        // only while isRecording is true, so it starts/stops/resets with it.
                        // Full screen width (no horizontal padding) and taller container
                        // so the shape reads as prominently as in recording.png.
                        RecordingWaveform(
                            volume = recording.energy.lastOrNull() ?: 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    isTranscribing -> {
                        // Kept inset at the original 32dp margin (unlike the recording
                        // waveform above) since the root Box no longer supplies that
                        // padding for the whole screen — this state's look is unchanged.
                        TranscribingIndicator(processingPhase = processingPhase)

                        // The text so far, growing as each piece of the recording is
                        // finished. A long recording otherwise leaves a still screen
                        // for minutes, which looks broken and invites a force quit.
                        val soFar = (dictationState as DictationState.Transcribing).textSoFar
                        if (soFar.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = soFar,
                                color = AvelisseColors.TextPrimary,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Button — changes appearance based on state
            when {
                isRecording -> {
                    // Terracotta stop button during recording (matches recording.png)
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(AvelisseColors.Secondary)
                            .clickable {
                                scope.launch {
                                    val result = dictationController?.confirmAndTranscribe()
                                    transcriptionResult = result ?: noResultLabel
                                    if (result != null) {
                                        onTranscribed(result)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = stringResource(R.string.recording_stop_cd),
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                isTranscribing -> {
                    // Disabled mic button during transcription
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(AvelisseColors.Surface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = AvelisseColors.TextSecondary,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                hasResult -> {
                    // The entry is already filed, so the only thing left to do is leave.
                    // Re-recording here would file a second entry for the same moment.
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(AvelisseColors.Primary)
                            .clickable(onClick = leave),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.recording_done),
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                else -> {
                    // Idle state: teal mic button to start recording
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(AvelisseColors.Primary)
                            .clickable {
                                dictationController?.startRecording()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = stringResource(R.string.recording_record_cd),
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }

            // Reserve space for label below the button
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    isRecording -> stringResource(R.string.recording_tap_to_stop)
                    hasResult -> stringResource(R.string.recording_done)
                    else -> ""
                },
                color = AvelisseColors.TextSecondary,
                fontSize = 13.sp,
            )
        }
    }
}

/**
 * "Recording..." label + timer shown above the waveform while isRecording is
 * true — shared by [RecordingScreen] and OnboardingTestRecordingScreen so both
 * stay visually identical for this state (matches recording.png).
 */
@Composable
fun RecordingLabelAndTimer(elapsedMs: Long, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.recording_recording_label),
            color = AvelisseColors.TextSecondary,
            fontSize = 20.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        val seconds = (elapsedMs / 1000).toInt()
        val minutes = seconds / 60
        val secs = seconds % 60
        Text(
            text = "%d:%02d".format(minutes, secs),
            color = AvelisseColors.TextPrimary,
            fontSize = 48.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Mic-reactive waveform shown while isRecording is true, with the AVELISSE Home
 * palette colors baked in — shared by [RecordingScreen] and
 * OnboardingTestRecordingScreen so both use identical shape/animation/colors
 * (matches recording.png). [volume] should be the caller's most recent mic
 * energy sample (see [dev.avelissesolutions.avelisse.core.service.DictationState.Recording.energy]).
 */
@Composable
fun RecordingWaveform(volume: Float, modifier: Modifier = Modifier) {
    WaveformBlob(
        volume = volume,
        modifier = modifier,
        color = AvelisseColors.Primary,
        edgeColor = AvelisseColors.Accent,
    )
}

/**
 * "Transcribing..." label + bars shown while isTranscribing is true, with the
 * AVELISSE Home palette colors baked in — shared by [RecordingScreen] and
 * OnboardingTestRecordingScreen so both stay visually identical for this state.
 */
@Composable
fun TranscribingIndicator(processingPhase: Double, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.recording_transcribing),
            color = AvelisseColors.TextSecondary,
            fontSize = 17.sp,
            modifier = Modifier.padding(horizontal = 32.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Colors overridden to the AVELISSE Home palette (teal inner bars, muted
        // secondary-text outer bars) instead of the default blue/white — the
        // app's default dark theme would otherwise render near-invisible white
        // bars against this screen's ivory background.
        WaveformBars(
            energyLevels = emptyList(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(80.dp),
            isProcessing = true,
            processingPhase = processingPhase,
            innerColor = AvelisseColors.Primary,
            outerColor = AvelisseColors.TextSecondary,
        )
    }
}
