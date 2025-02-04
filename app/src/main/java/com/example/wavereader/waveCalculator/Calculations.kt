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
    val peakFrequency = calculateFft(verticalAcceleration, samplingRate)
    return if (peakFrequency > 0) 1 / peakFrequency else 0f
}

// Use Fast Fourier Transform to transform from temporal domain to frequency domain
// Return peak frequency
fun calculateFft(verticalAcceleration: List<Float>, samplingRate: Float): Float {

    if (verticalAcceleration.isEmpty()) {
        println("Warning: data is empty!")
        return 0f
    }

    val n = verticalAcceleration.size
    val fft = FloatFFT_1D(n.toLong())

    //Create array twice the size and add imaginary numbers for complex array
    val fftArray = FloatArray(2 * n)
    for (i in verticalAcceleration.indices) {
        fftArray[2 * i] = verticalAcceleration[i]
        fftArray[2 * i + 1] = 0f
    }

    // Perform FFT
    fft.realForward(fftArray)

    println("FFT Data After Transform: ${fftArray.contentToString()}")

    //Get magnitudes and find peak frequency
    var maxMagnitude = 0f
    var peakIndex = 0

    for (i in 1 until n / 2) { // Only consider positive frequencies
        val real = fftArray[2 * i]
        val imaginary = fftArray[2 * i + 1]
        val magnitude = sqrt(real.pow(2) + imaginary.pow(2))

        if (magnitude > maxMagnitude) {
            maxMagnitude = magnitude
            peakIndex = i
        }
    }
    // Convert index to frequency
    return peakIndex * samplingRate / n
}

// TODO Unsure if needed, possibly for direction
fun getPeakAcceleration(acceleration: List<Float>): Float {
    return acceleration.maxOrNull() ?: 0f
}

//TODO
// Direction calculation
// Temporary solution finding angle of dominant motion
fun calculateWaveDirection(acceleration: List<Array<Float>>): Float {
    val ax = mutableListOf<Float>()
    val ay = mutableListOf<Float>()
    //separate out x and y values
    for (i in acceleration.indices){
        ax.add(acceleration[i][0])
        ay.add(acceleration[i][1])
    }
    val avgX = ax.average().toFloat()
    val avgY = ay.average().toFloat()

    return atan2(avgY, avgX) * (180 / Math.PI.toFloat())
}