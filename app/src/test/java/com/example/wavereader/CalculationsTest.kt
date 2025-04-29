package com.example.wavereader

import com.example.wavereader.utils.calculateWaveDirection
import com.example.wavereader.utils.calculateWaveHeight
import com.example.wavereader.utils.calculateWavePeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class CalculationsTest {

    @Test
    fun testCalculateWaveHeight_flatData() {
        val acceleration = List(100) { 0f } // No acceleration
        val dt = 0.1f

        val result = calculateWaveHeight(acceleration, dt)
        assertEquals(0f, result, 0.001f)
    }

    @Test
    fun testCalculateWaveHeight_withConstantAcceleration() {
        val acceleration = List(100) { 9.8f } // Constant acceleration
        val dt = 0.1f

        val expected = preciseDoubleIntegrationHeight(acceleration, dt)
        val result = calculateWaveHeight(acceleration, dt)

        assertEquals(expected, result, 0.001f)
    }

    @Test
    fun testCalculateWavePeriod_knownFrequency() {
        val samplingRate = 1000f // Increased resolution
        val frequency = 2f
        val size = 2000 // More samples

        val data = generateSineWave(frequency, samplingRate, size)

        val result = calculateWavePeriod(data, samplingRate)
        val expectedPeriod = 1 / frequency

        assertEquals(expectedPeriod, result, 0.05f) // Allow small margin
    }

    @Test
    fun testCalculateWavePeriod_multipleFrequencies() {
        val samplingRate = 1000f
        val size = 2000

        val frequencies = listOf(1f, 5f, 10f)

        frequencies.forEach { frequency ->
            val data = generateSineWave(frequency, samplingRate, size)
            val result = calculateWavePeriod(data, samplingRate)
            val expectedPeriod = 1 / frequency

            assertEquals("Failed at frequency $frequency", expectedPeriod, result, 0.05f)
        }
    }

    @Test
    fun testCalculateWaveDirection_knownPhaseShift() {
        val samplingRate = 1000f
        val frequency = 2f
        val size = 2000

        val accelX = generateSineWave(frequency, samplingRate, size)
        val phaseShift = (PI / 2).toFloat() // 90 degrees
        val accelY = generateSineWave(frequency, samplingRate, size, phaseShift)

        val result = calculateWaveDirection(accelX, accelY)

        val expectedDirection = 90f
        assertEquals(expectedDirection, result, 5f) // Allow margin
    }

    @Test
    fun testCalculateWaveDirection_noData() {
        val result = calculateWaveDirection(emptyList(), emptyList())
        assertEquals(0f, result, 0.001f)
    }

    // ----- Helpers -----

    private fun generateSineWave(
        frequency: Float,
        samplingRate: Float,
        size: Int,
        phaseShift: Float = 0f
    ): List<Float> {
        return List(size) { i ->
            sin(2 * PI * frequency * i / samplingRate + phaseShift).toFloat()
        }
    }

    private fun preciseDoubleIntegrationHeight(acceleration: List<Float>, dt: Float): Float {
        val velocity = mutableListOf(0f)
        val displacement = mutableListOf(0f)

        for (i in 1 until acceleration.size) {
            val v = velocity.last() + acceleration[i] * dt
            velocity.add(v)

            val d = displacement.last() + v * dt
            displacement.add(d)
        }

        val max = displacement.maxOrNull() ?: 0f
        val min = displacement.minOrNull() ?: 0f
        return max - min
    }
}