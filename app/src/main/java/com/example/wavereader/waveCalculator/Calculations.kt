package com.example.wavereader.waveCalculator

import org.apache.commons.math3.complex.Complex
import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.pow
import kotlin.math.sqrt


// Wave height calculation
fun calculateWaveHeight(acceleration: List<Float>): Float {

    // Calculate peak-to-peak distance
    val maxAccel = acceleration.maxOrNull() ?: 0f
    val minAccel = acceleration.minOrNull() ?: 0f
    return maxAccel - minAccel
}

// Wave Period = 1/f
fun calculateWavePeriod(dominantFrequency: Float): Float {
    return if (dominantFrequency > 0) {
        1/dominantFrequency
    } else 0f
}

// Use Fast Fourier Transform to transform from temporal domain to frequency domain
fun calculateFft(verticalAcceleration: List<Float>): List<Float> {
    if (verticalAcceleration.isEmpty()) {
        println("Warning: Input list is empty!")
        return emptyList()
    }

    val n = verticalAcceleration.size
    //val dataArray = verticalAcceleration.map { Complex(it.toDouble(), 0.0) }
    val dataArray = verticalAcceleration.toFloatArray().copyOf(n * 2)

    //println("Input to FFT: ${dataArray.contentToString()}")

    // Perform FFT
    val fft = FloatFFT_1D(n.toLong())
    fft.complexForward(dataArray)

    //println("FFT Data After Transform: ${dataArray.contentToString()}")

    //Get Magnitude
    val magnitudes = FloatArray(n)
    for (i in 0 until (n)) {
        val real = dataArray[2 * i]     // Real part
        val imag = dataArray[2 * i + 1] // Imaginary part
        magnitudes[i] = sqrt(real.pow(2) + imag.pow(2)) // Magnitude
    }

    println("Computed Magnitude List: ${magnitudes.contentToString()}")
    return magnitudes.asList()
}

// TODO Unsure if needed
fun calculateMagnitudes(acceleration: List<Array<Float>>): List<Float>{
    return acceleration.map { a ->
        sqrt(a[0].pow(2) + a[1].pow(2) + a[2].pow(2))
    }.also { println("Magnitude: $it") }
}

// Dominant Frequency calculation
fun calculateDominantFrequency(fft: List<Float>, samplingRate: Float): Float {
    val maxNum = fft.maxOrNull() ?: return 0f
    val maxIndex = fft.indexOf(maxNum)
    val fftSize = fft.size

    return (maxIndex * samplingRate) / fftSize
}

// TODO Unsure if needed, possibly for direction
fun getPeakAcceleration(acceleration: List<Float>): Float {
    return acceleration.maxOrNull() ?: 0f
}

//TODO
// Direction calculation
// Variables to store cumulative tilt values
fun calculateWaveDirection(rotationX: Float, rotationY: Float, rotationZ: Float, dt: Float) {

}