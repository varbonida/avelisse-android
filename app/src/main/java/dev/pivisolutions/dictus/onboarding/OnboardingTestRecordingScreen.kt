package dev.pivisolutions.dictus.onboarding

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
import dev.pivisolutions.dictus.R
import dev.pivisolutions.dictus.core.service.DictationController
import dev.pivisolutions.dictus.core.service.DictationState
import dev.pivisolutions.dictus.core.theme.DictusColors
import dev.pivisolutions.dictus.core.ui.HomeGlassCard
import dev.pivisolutions.dictus.core.ui.WaveformDriver
import dev.pivisolutions.dictus.recording.RecordingLabelAndTimer
import dev.pivisolutions.dictus.recording.RecordingWaveform
import dev.pivisolutions.dictus.recording.TranscribingIndicator
import dev.pivisolutions.dictus.ui.onboarding.OnboardingCTAButton
import dev.pivisolutions.dictus.ui.onboarding.OnboardingProgressDots
import dev.pivisolutions.dictus.ui.onboarding.accentGradient
import kotlinx.coroutines.launch

/**
 * Onboarding Step 6 — Test recording screen.
 *
 * Uses a layered Box layout so the mic/stop button stays at a fixed position
 * (200dp from bottom) regardless of state. Three layers:
 * 1. Upper content (text, waveform, timer) — centered above the button
 * 2. Button layer — fixed 200dp from bottom, stable across all states
 * 3. Bottom bar (Passer/Continuer + progress dots) — pinned to bottom
 *
 * @param dictationController Controller for the DictationService (may be null if not yet bound).
 * @param onNext Called when the user taps "Continuer" after seeing the result, or "Passer" to skip.
 */
@Composable
fun OnboardingTestRecordingScreen(
    dictationController: DictationController?,
    onNext: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val dictationState by dictationController?.state?.collectAsState()
        ?: remember { mutableStateOf(DictationState.Idle as DictationState) }

    var transcriptionResult by remember { mutableStateOf<String?>(null) }
    var copied by remember { mutableStateOf(false) }

    // Processing animation driver for the transcribing state.
    // Uses WaveformDriver.processingEnergy() — same formula as iOS BrandWaveformDriver.
    val processingDriver = remember {
        WaveformDriver().apply { isProcessing = true }
    }
    val processingPhase by processingDriver.processingPhase.collectAsState()

    // Run the processing animation loop when transcribing.
    if (dictationState is DictationState.Transcribing) {
        LaunchedEffect(Unit) {
            processingDriver.runLoop()
        }
    }

    val hasResult = transcriptionResult != null
    val isRecording = dictationState is DictationState.Recording
    val isTranscribing = dictationState is DictationState.Transcribing

    // No horizontal padding on the root Box: the recording waveform bleeds to the
    // full screen width, matching RecordingScreen. Everything else applies its
    // own 32dp horizontal padding below instead.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DictusColors.HomeBackground),
    ) {
        // ── Layer 1: Upper content (idle text, "Recording..." + timer, or result card) ──
        when {
            hasResult -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 32.dp)
                        .padding(top = 180.dp, bottom = 240.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_test_recording_result),
                        color = DictusColors.HomeTextPrimary,
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
                            color = DictusColors.HomeTextPrimary,
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
                                contentDescription = stringResource(R.string.onboarding_test_recording_cd_copy),
                                tint = if (copied) DictusColors.HomeAccentSecondary else DictusColors.HomeTextSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            isRecording -> {
                // "Recording..." label + timer, centered above the waveform (matches RecordingScreen).
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
                // Idle: title + subtitle centered
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 32.dp)
                        .padding(top = 200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_test_recording_title),
                        color = DictusColors.HomeTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.onboarding_test_recording_body),
                        color = DictusColors.HomeTextSecondary,
                        fontSize = 15.sp,
                        lineHeight = (15 * 1.5).sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // ── Layer 2: Bottom block (waveform + timer + button) ──
        // For recording/transcribing: waveform, timer, and button are grouped together
        // in the lower portion of the screen (matching iOS layout).
        // For idle: just the mic button.
        if (!hasResult) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Waveform (only during recording/transcribing) — shared with
                // RecordingScreen so both screens look/animate identically.
                when {
                    isRecording -> {
                        val recording = dictationState as DictationState.Recording

                        RecordingWaveform(
                            volume = recording.energy.lastOrNull() ?: 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    isTranscribing -> {
                        TranscribingIndicator(processingPhase = processingPhase)

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Button (same position in all non-result states)
                when {
                    isRecording -> {
                        // Terracotta stop button during recording (matches RecordingScreen).
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(DictusColors.HomeAccentSecondary)
                                .clickable {
                                    scope.launch {
                                        val result = dictationController?.confirmAndTranscribe()
                                        transcriptionResult = result ?: context.getString(R.string.onboarding_test_recording_no_result)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = stringResource(R.string.onboarding_test_recording_cd_stop),
                                tint = Color.White,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }

                    isTranscribing -> {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(DictusColors.HomeSurface),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = DictusColors.HomeTextSecondary,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(DictusColors.HomeAccent)
                                .clickable {
                                    dictationController?.startRecording()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.onboarding_test_recording_cd_record),
                                tint = Color.White,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }
                }

                // Reserve space for label
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRecording) stringResource(R.string.onboarding_test_recording_tap_to_stop) else "",
                    color = DictusColors.HomeTextSecondary,
                    fontSize = 13.sp,
                )
            }
        }

        // ── Layer 3: Bottom bar (Passer/Continuer + dots) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (hasResult) {
                OnboardingCTAButton(
                    text = stringResource(R.string.onboarding_test_recording_cta_continue),
                    onClick = onNext,
                    gradient = accentGradient,
                )
            } else {
                Spacer(modifier = Modifier.height(56.dp))
            }

            Spacer(modifier = Modifier.padding(top = 24.dp))

            OnboardingProgressDots(currentStep = 6)
        }
    }
}
