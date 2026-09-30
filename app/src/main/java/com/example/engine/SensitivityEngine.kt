package com.example.engine

import com.example.model.CalibrationStats
import com.example.model.PresetType
import kotlin.math.roundToInt
import kotlin.math.sqrt

object SensitivityEngine {

    const val MIN_SENS = 0.10f
    const val MAX_SENS = 3.00f
    const val SENS_STEP = 0.05f

    data class SensitivityState(
        val sensX: Float = 1.00f,
        val sensY: Float = 1.00f,
        val isLinked: Boolean = true,
        val linkRatio: Float = 1.00f, // Y / X ratio
        val preset: PresetType = PresetType.MEDIUM
    )

    fun calculateLinkedY(newX: Float, ratio: Float): Float {
        val y = newX * ratio
        return ((y * 100f).roundToInt() / 100f).coerceIn(MIN_SENS, MAX_SENS)
    }

    fun calculateLinkedX(newY: Float, ratio: Float): Float {
        if (ratio <= 0f) return newY
        val x = newY / ratio
        return ((x * 100f).roundToInt() / 100f).coerceIn(MIN_SENS, MAX_SENS)
    }

    fun getPresetValues(preset: PresetType): Pair<Float, Float> {
        return when (preset) {
            PresetType.LOW -> 0.60f to 0.50f
            PresetType.MEDIUM -> 1.00f to 1.00f
            PresetType.HIGH -> 1.80f to 1.50f
            PresetType.CUSTOM -> 1.00f to 1.00f
        }
    }

    fun detectPreset(sensX: Float, sensY: Float): PresetType {
        val x = (sensX * 100).roundToInt() / 100f
        val y = (sensY * 100).roundToInt() / 100f
        return when {
            x == 0.60f && y == 0.50f -> PresetType.LOW
            x == 1.00f && y == 1.00f -> PresetType.MEDIUM
            x == 1.80f && y == 1.50f -> PresetType.HIGH
            else -> PresetType.CUSTOM
        }
    }

    /**
     * Map sensitivity multiplier (0.1x to 3.0x) to Android system pointer_speed [-7 .. +7]
     * 1.0x -> 0 (default)
     * 0.1x -> -7
     * 3.0x -> +7
     */
    fun mapSensitivityToPointerSpeed(sens: Float): Int {
        val clamped = sens.coerceIn(MIN_SENS, MAX_SENS)
        return if (clamped < 1.0f) {
            val progress = (clamped - MIN_SENS) / (1.0f - MIN_SENS) // 0..1
            (-7 + progress * 7).roundToInt()
        } else {
            val progress = (clamped - 1.0f) / (MAX_SENS - 1.0f) // 0..1
            (progress * 7).roundToInt()
        }.coerceIn(-7, 7)
    }

    /**
     * Compute target DPI via Shizuku for touch travel scaling
     */
    fun calculateTargetDpi(baseDpi: Int, sensX: Float, sensY: Float): Int {
        val avgSens = (sensX + sensY) / 2.0f
        if (avgSens <= 0.05f) return baseDpi
        // Higher sensitivity = lower DPI = more virtual pixels per physical inch
        val scaled = (baseDpi / avgSens).roundToInt()
        // Clamp to safe Android limits (120 to 640 DPI)
        return scaled.coerceIn(120, 640)
    }

    /**
     * Transform raw input delta through configured X/Y sensitivity multipliers
     */
    fun transformDelta(
        rawDx: Float,
        rawDy: Float,
        sensX: Float,
        sensY: Float
    ): Pair<Float, Float> {
        val transformedDx = rawDx * sensX
        val transformedDy = rawDy * sensY
        return transformedDx to transformedDy
    }

    /**
     * Process touch calibration step
     */
    fun processCalibrationInput(
        currentStats: CalibrationStats,
        rawX: Float,
        rawY: Float,
        rawDx: Float,
        rawDy: Float,
        sensX: Float,
        sensY: Float
    ): CalibrationStats {
        val (txDx, txDy) = transformDelta(rawDx, rawDy, sensX, sensY)
        val rawDist = sqrt((rawDx * rawDx + rawDy * rawDy).toDouble()).toFloat()
        val txDist = sqrt((txDx * txDx + txDy * txDy).toDouble()).toFloat()

        val now = System.currentTimeMillis()
        val timeDiff = if (currentStats.lastEventTimestamp > 0) now - currentStats.lastEventTimestamp else 16L
        val hz = if (timeDiff > 0) (1000L / timeDiff).toInt().coerceIn(1, 240) else 60

        val newSampleCount = currentStats.sampleCount + 1
        // Verified if we received real movement and non-zero transformed delta
        val verified = newSampleCount > 3 && (txDist > 0.1f)

        return currentStats.copy(
            rawX = rawX,
            rawY = rawY,
            rawDeltaX = rawDx,
            rawDeltaY = rawDy,
            transformedDeltaX = txDx,
            transformedDeltaY = txDy,
            cumulativeRawDistance = currentStats.cumulativeRawDistance + rawDist,
            cumulativeTransformedDistance = currentStats.cumulativeTransformedDistance + txDist,
            sampleCount = newSampleCount,
            eventFrequencyHz = hz,
            lastEventTimestamp = now,
            isVerified = verified
        )
    }
}
