package com.example.wavereader.utils

import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Sensor View Model for control the sensor manager and processing data
 * Resources: For calculations and data processing
 * https://www.ndbc.noaa.gov/faq/wavecalc.shtml
 * https://www.ndbc.noaa.gov/wavemeas.pdf
 */

// The significant wave height is derived from the zeroth spectral moment m0 represents the total wave energy.
//
fun calculateSignificantWaveHeight(m0: Float): Float {
    return 4 * sqrt(m0)
}


// Average Wave Period
//
fun calculateAveragePeriod(m0: Float, m1: Float): Float {
    return if (m1 != 0f) m0 / m1 else 0f
}

// Hanning to reduce spectral leaks
fun hanningWindow(data: List<Float>): List<Float> {
    val n = data.size
    return data.mapIndexed { i, value ->
        val multiplier = 0.5f * (1 - kotlin.math.cos(2 * Math.PI * i / (n - 1))).toFloat()
        value * multiplier
    }
}

// Calculate FFT
// Use JTransforms Fast Fourier Transform to transform from temporal domain to frequency domain
fun getFft(data: List<Float>, n: Int): FloatArray {

    val fft = FloatFFT_1D(n.toLong())

    //Create array twice the size and add imaginary numbers for complex array
    val fftArray = FloatArray(2*n)
    for (i in data.indices) {
        fftArray[ 2 * i ] = data[i]
        fftArray[ 2 * i + 1 ] = 0f
    }

    //Perform FFT
    fft.realForward(fftArray)
    println("FFT Data After Transform: ${fftArray.contentToString()}")

    return fftArray
}

// Get Index of max magnitude
fun getPeakIndex(data: FloatArray, n: Int): Int {
    //Get magnitudes
    var maxMagnitude = 0f
    var peakIndex = 0

    for (i in 1 until n / 2) {
        val real = data[2 * i]
        val imaginary = data[2 * i + 1]
        val magnitude = sqrt(real.pow(2) + imaginary.pow(2))

        if (magnitude > maxMagnitude) {
            maxMagnitude = magnitude
            peakIndex = i
        }
    }
    return peakIndex
}

// Calcualtes phase difference between two orthogonal acceleration FFTs to estimate wave direction
fun calculateWaveDirection(accelX: List<Float>, accelY: List<Float>): Float {
    if (accelX.isEmpty() || accelY.isEmpty()) {
        println("Warning: data is empty!")
        return 0f
    }
    val n = accelX.size

    val windowedX = hanningWindow(accelX)
    val windowedY = hanningWindow(accelY)

    val fftX = getFft(windowedX, n)
    val fftY = getFft(windowedY, n)

    val peakX = getPeakIndex(fftX, n)
    val peakY = getPeakIndex(fftY, n)

    // Get phase angles at the dominant frequency
    val phaseX = atan2(fftX[2 * peakX + 1], fftX[2 * peakX])
    val phaseY = atan2(fftY[2 * peakY + 1], fftY[2 * peakY])

    val phaseDifference = phaseY - phaseX

    // Compute wave direction in degrees
    var waveDirection = Math.toDegrees(phaseDifference.toDouble()).toFloat()
    if (waveDirection < 0) waveDirection += 360 // Normalize to 0-360°

    return waveDirection
}

//converts FFT output into a power spectrum (density per frequency band), normalized by FFT size.
fun computeSpectralDensity(fft: FloatArray, n: Int): List<Float> {
    val halfN = n / 2
    return (0 until halfN).map { i ->
        val re = fft[2 * i]
        val im = fft[2 * i + 1]
        (re * re + im * im) / n
    }
}

//Each moment is calculated as the weighted sum of spectral density across frequencies
fun calculateSpectralMoments(spectrum: List<Float>, samplingRate: Float): Triple<Float, Float, Float> {
    val df = samplingRate / (2 * spectrum.size) // frequency resolution
    val freq = spectrum.indices.map { it * df }

    var m0 = 0f
    var m1 = 0f
    var m2 = 0f

    for (i in spectrum.indices) {
        val S = spectrum[i]
        val f = freq[i]
        m0 += S * df
        m1 += S * f * df
        m2 += S * f * f * df
    }

    return Triple(m0, m1, m2)
}

/*
* Significant height is a direct function of sqrt(m0)
Zero-crossing period offers a stable baseline for comparisons (needed?)
* */
fun computeWaveMetricsFromSpectrum(m0: Float, m1: Float, m2: Float): Triple<Float, Float, Float> {
    val significantHeight = calculateSignificantWaveHeight(m0)
    val avgPeriod = calculateAveragePeriod(m0, m1)
    val zeroCrossingPeriod = if (m2 != 0f) sqrt(m0 / m2) else 0f
    return Triple(significantHeight, avgPeriod, zeroCrossingPeriod)
}
