package dev.pivisolutions.dictus.whisper

import android.os.Build
import timber.log.Timber
import java.io.File

/**
 * JNI declarations for whisper.cpp native library.
 *
 * Function names must match jni.c exactly (package + class encoding).
 * System.loadLibrary("whisper") loads libwhisper.so at class init.
 *
 * WHY multiple library variants: whisper.cpp builds optimized variants for
 * different ARM instruction sets. ARM v8.2a fp16 (whisper_v8fp16_va) is fastest
 * on modern devices like Pixel 4 (Cortex-A76).
 *
 * WHY a /proc/cpuinfo feature check (not just try/catch on loadLibrary): a shared
 * library compiled with -march=armv8.2-a+fp16 is a valid ELF for ANY arm64-v8a
 * device, so System.loadLibrary() succeeds even on CPUs that predate ARMv8.2-A
 * (e.g. Cortex-A73/A53, common on budget Snapdragon SoCs). The mismatch only
 * surfaces as a SIGILL crash the first time an fp16 instruction actually executes,
 * deep inside whisper_full_with_state. We must instead check for the "fphp" CPU
 * feature flag before attempting the fp16 variant, same as upstream whisper.cpp's
 * reference Android sample (third_party/whisper.cpp/examples/whisper.android).
 */
class WhisperLib {
    companion object {
        init {
            // Load the best available variant: fp16 > vfpv4 > default
            var loaded = false

            val abi = Build.SUPPORTED_ABIS.firstOrNull()
            val cpuFeatures = cpuInfoFeatures()

            // ARM v8.2a fp16 (Cortex-A76+ including Pixel 4) — only on CPUs that
            // actually implement the fp16 vector arithmetic extension.
            if (!loaded && abi == "arm64-v8a" && cpuFeatures?.contains("fphp") == true) {
                try {
                    System.loadLibrary("whisper_v8fp16_va")
                    Timber.d("Loaded whisper_v8fp16_va (ARM fp16)")
                    loaded = true
                } catch (e: UnsatisfiedLinkError) {
                    Timber.d("whisper_v8fp16_va not available")
                }
            }

            // ARMv7 NEON fallback — only on CPUs that report vfpv4 support.
            if (!loaded && abi == "armeabi-v7a" && cpuFeatures?.contains("vfpv4") == true) {
                try {
                    System.loadLibrary("whisper_vfpv4")
                    Timber.d("Loaded whisper_vfpv4 (ARMv7 NEON)")
                    loaded = true
                } catch (e: UnsatisfiedLinkError) {
                    Timber.d("whisper_vfpv4 not available")
                }
            }

            // Default fallback: no CPU-specific instructions, safe on every device.
            if (!loaded) {
                System.loadLibrary("whisper")
                Timber.d("Loaded whisper (default)")
            }
        }

        /** Returns the raw contents of /proc/cpuinfo, or null if it can't be read. */
        private fun cpuInfoFeatures(): String? = try {
            File("/proc/cpuinfo").readText()
        } catch (e: Exception) {
            Timber.d(e, "Couldn't read /proc/cpuinfo for CPU feature detection")
            null
        }

        external fun initContext(modelPath: String): Long
        external fun freeContext(contextPtr: Long)
        external fun fullTranscribe(contextPtr: Long, numThreads: Int, audioData: FloatArray, language: String)
        external fun getTextSegmentCount(contextPtr: Long): Int
        external fun getTextSegment(contextPtr: Long, index: Int): String
        external fun getSystemInfo(): String
    }
}
