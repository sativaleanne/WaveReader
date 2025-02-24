package com.example.wavereader.waveCalculator

import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


// Wave height calculation
fun calculateWaveHeight(acceleration: List<Float>, dt: Float): Float {

    val verticalDisplacement = doubleIntegrate(acceleration, dt)
    // Calculate peak-to-peak distance
    val max = verticalDisplacement.maxOrNull() ?: 0f
    val min = verticalDisplacement.minOrNull() ?: 0f
    return max - min
}

// Double integration for acceleration to velocity to displacement
fun doubleIntegrate(acceleration: List<Float>, dt: Float): List<Float> {
    val velocity = mutableListOf(0f)
    val displacement = mutableListOf(0f)

    for (i in 1 until acceleration.size) {
        val v = velocity.last() + acceleration[i] * dt
        velocity.add(v)

        val d = displacement.last() + v * dt
        displacement.add(d)
    }

    return displacement
}

// Wave Period = 1/f
fun calculateWavePeriod(verticalAcceleration: List<Float>, samplingRate: Float): Float {
    if (verticalAcceleration.isEmpty()) {
        println("Data is empty!")
        return 0f
    }
    val n = verticalAcceleration.size
    val fft = getFft(verticalAcceleration, n)
    val peakFrequency = getPeakIndex(fft, n) * samplingRate / n
    return if (peakFrequency > 0) 1 / peakFrequency else 0f
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

    for (i in 1 until n / 2) { // Only consider positive frequencies
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

//TODO
// Direction calculation
// Temporary solution using horizontal acceleration
fun calculateWaveDirection(accelX: List<Float>, accelY: List<Float>): Float {
    if (accelX.isEmpty() || accelY.isEmpty()) {
        println("Warning: data is empty!")
        return 0f
    }
    val n = accelX.size
    val fftX = getFft(accelX, n)
    val fftY = getFft(accelY, n)

    val peakX = getPeakIndex(fftX, n)
    val peakY = getPeakIndex(fftY, n)

    // Get phase angles at the dominant frequency
    val phaseX = atan2(fftX[2 * peakX + 1], fftX[2 * peakY])
    val phaseY = atan2(fftY[2 * peakY + 1], fftY[2 * peakY])

    val phaseDifference = phaseY - phaseX

    // Compute wave direction in degrees
    var waveDirection = Math.toDegrees(phaseDifference.toDouble()).toFloat()
    if (waveDirection < 0) waveDirection += 360 // Normalize to 0-360°

    return waveDirection
}
