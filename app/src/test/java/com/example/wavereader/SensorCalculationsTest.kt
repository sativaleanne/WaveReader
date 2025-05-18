package com.example.wavereader

import com.example.wavereader.model.MeasuredWaveData
import com.example.wavereader.utils.*
import org.junit.Assert.*
import org.junit.Test

class SensorCalculationsTest {

    private fun generateSineWave(
        freqHz: Float,
        sampleRate: Float,
        durationSec: Float,
        amplitude: Float = 1f
    ): List<Float> {
        val totalSamples = (sampleRate * durationSec).toInt()
        val dt = 1 / sampleRate
        return List(totalSamples) { i ->
            amplitude * kotlin.math.sin(2 * Math.PI * freqHz * i * dt).toFloat()
        }
    }

    @Test
    fun testHanningWindowPreservesLengthAndSmoothsEdges() {
        val input = List(100) { 1f }
        val windowed = hanningWindow(input)
        assertEquals("Output length should match input", input.size, windowed.size)
        assertTrue("First value should be 0", windowed.first() < 0.01f)
        assertTrue("Last value should be 0", windowed.last() < 0.01f)
    }

    @Test
    fun testComputeSpectralDensity() {
        val signal = generateSineWave(0.5f, 50f, 20f)
        val fft = getFft(hanningWindow(signal), signal.size)
        val spectrum = computeSpectralDensity(fft, signal.size)

        assertEquals("Spectrum size should be half of input size", signal.size / 2, spectrum.size)
        assertTrue("All spectral densities should be non-negative", spectrum.all { it >= 0f })
    }

    @Test
    fun testCalculateSpectralMomentsBehavior() {
        val signal = generateSineWave(0.5f, 50f, 20f)
        val spectrum = computeSpectralDensity(getFft(hanningWindow(signal), signal.size), signal.size)
        val (m0, m1, m2) = calculateSpectralMoments(spectrum, 50f)

        assertTrue("m0 should be > 0", m0 > 0f)
        assertTrue("m1 should be > 0", m1 > 0f)
        assertTrue("m2 should be > 0", m2 > 0f)
    }

    @Test
    fun testSlopeAndForecast() {
        val data = listOf(1f, 2f, 3f, 4f, 5f)
        assertEquals("Linear slope should be 1", 1f, slope(data), 0.01f)
        val forecast = forecastNext(data)
        assertNotNull("Forecast should return a value", forecast)
        assertEquals("Next forecast should be 6", 6f, forecast!!, 0.1f)
    }

    @Test
    fun testZScoreOfLast() {
        val values = listOf(1f, 2f, 3f, 4f, 10f) // Last is an outlier
        val z = values.zScore()
        assertTrue("Z-score should be greater than 1.0", z > 1f)
    }

    @Test
    fun testStandardDeviation() {
        val values = listOf(2f, 4f, 4f, 4f, 5f, 5f, 7f, 9f)
        val std = values.standardDeviation()
        assertEquals("Expected sample-based std dev", 2f, std, 0.1f)
    }

    @Test
    fun testMovingAverage() {
        val values = listOf(1f, 2f, 3f, 4f, 5f)
        val avg = movingAverage(values, 3)
        assertEquals("Should return 3 average points", 3, avg.size)
        assertEquals("Middle average should be 3", 3f, avg[1], 0.01f)
    }

    @Test
    fun testSpectralMomentsAndWaveMetrics() {
        val sampleRate = 50f
        val signal = generateSineWave(freqHz = 0.5f, sampleRate = sampleRate, durationSec = 20f)

        val window = hanningWindow(signal)
        val fft = getFft(window, window.size)
        val spectrum = computeSpectralDensity(fft, window.size)
        val (m0, m1, m2) = calculateSpectralMoments(spectrum, sampleRate)
        val (height, avgPeriod, zeroPeriod) = computeWaveMetricsFromSpectrum(m0, m1, m2)

        assertTrue("Significant height should be > 0", height > 0f)
        assertTrue("Average period should be > 0", avgPeriod > 0f)
        assertTrue("Zero-crossing period should be > 0", zeroPeriod > 0f)
    }

    @Test
    fun testWaveDirectionFromFFT() {
        val sampleRate = 50f
        val duration = 20f
        val freq = 0.5f
        val accelX = generateSineWave(freq, sampleRate, duration)
        val accelY = generateSineWave(freq, sampleRate, duration, amplitude = 0.5f)

        val direction = calculateWaveDirection(accelX, accelY)

        assertTrue("Direction should be within 0-360", direction in 0f..360f)
    }

    @Test
    fun testRefinedBigWavePrediction() {
        val data = listOf(
            MeasuredWaveData(1.0f, 5f, 90f, 0f),
            MeasuredWaveData(1.5f, 5f, 95f, 1f),
            MeasuredWaveData(2.0f, 5f, 100f, 2f),
            MeasuredWaveData(2.7f, 5f, 105f, 3f),
            MeasuredWaveData(3.3f, 5f, 110f, 4f),
            MeasuredWaveData(4.0f, 5f, 115f, 5f)
        )

        val confidence = nextBigWaveConfidence(data)
        println("Refined Confidence: $confidence")

        assertTrue("Should predict next big wave", predictNextBigWave(data))
    }
}