package com.example

import com.example.engine.SensitivityEngine
import com.example.model.PresetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testSensitivityDeltaTransformation() {
        val rawDx = 10f
        val rawDy = 20f
        val sensX = 1.5f
        val sensY = 0.8f

        val (txDx, txDy) = SensitivityEngine.transformDelta(rawDx, rawDy, sensX, sensY)

        assertEquals(15f, txDx, 0.001f)
        assertEquals(16f, txDy, 0.001f)
    }

    @Test
    fun testSensitivityLinkCalculation() {
        val x = 1.20f
        val ratio = 0.75f // 4:3
        val y = SensitivityEngine.calculateLinkedY(x, ratio)
        assertEquals(0.90f, y, 0.01f)

        val recoveredX = SensitivityEngine.calculateLinkedX(y, ratio)
        assertEquals(1.20f, recoveredX, 0.01f)
    }

    @Test
    fun testPresets() {
        val (lowX, lowY) = SensitivityEngine.getPresetValues(PresetType.LOW)
        assertEquals(0.60f, lowX, 0.001f)
        assertEquals(0.50f, lowY, 0.001f)

        val detected = SensitivityEngine.detectPreset(lowX, lowY)
        assertEquals(PresetType.LOW, detected)
    }

    @Test
    fun testPointerSpeedMapping() {
        val neutral = SensitivityEngine.mapSensitivityToPointerSpeed(1.0f)
        assertEquals(0, neutral)

        val min = SensitivityEngine.mapSensitivityToPointerSpeed(0.10f)
        assertEquals(-7, min)

        val max = SensitivityEngine.mapSensitivityToPointerSpeed(3.00f)
        assertEquals(7, max)
    }

    @Test
    fun testTargetDpiCalculation() {
        val baseDpi = 420
        val targetDpi = SensitivityEngine.calculateTargetDpi(baseDpi, 1.5f, 1.5f)
        assertEquals(280, targetDpi)
        assertTrue(targetDpi in 120..640)
    }
}
