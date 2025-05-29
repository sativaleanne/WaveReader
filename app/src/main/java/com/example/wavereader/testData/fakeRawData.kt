package com.example.wavereader.testData

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Helper degrees to radians
 */
fun degreesToRadians(degrees: Double): Double {
    return degrees * PI / 180
}

/**
 * Gaussian noise from Kotlin Random
 */
fun Random.nextGaussian(): Double {
    var u: Double
    var v: Double
    var s: Double
    do {
        u = nextDouble() * 2 - 1
        v = nextDouble() * 2 - 1
        s = u * u + v * v
    } while (s >= 1 || s == 0.0)
    return u * kotlin.math.sqrt(-2.0 * kotlin.math.ln(s) / s)
}

/**
 * Simulates raw sensor data from acceleration.
 *
 * @param freq Wave frequency in Hz
 * @param sampleRate Sensor sample rate in Hz
 * @param duration Duration of signal
 * @param amplitude Amplitude of wave acceleration (m/s²)
 * @param phaseOffset Phase offset between X and Y (in degrees)
 * @param noiseLevel Std dev of added Gaussian noise
 */
fun rawAccelerationData(
    freq: Float,
    sampleRate: Float = 50f,
    duration: Float = 20f,
    amplitude: Float = 1f,
    phaseOffset: Float = 90f,
    noiseLevel: Float = 0.05f
): Triple<List<Float>, List<Float>, List<Float>> {
    val samples = (sampleRate * duration).toInt()
    val dt = 1 / sampleRate
    val phaseOffsetRad = degreesToRadians(phaseOffset.toDouble()).toFloat()

    val accelX = mutableListOf<Float>()
    val accelY = mutableListOf<Float>()
    val accelZ = mutableListOf<Float>()

    repeat(samples) { i ->
        val t = i * dt
        val x = amplitude * sin(2 * PI.toFloat() * freq * t)
        val y = amplitude * sin(2 * PI.toFloat() * freq * t + phaseOffsetRad)
        val z = amplitude * sin(2 * PI.toFloat() * freq * t)

        accelX.add(x + Random.nextGaussian().toFloat() * noiseLevel)
        accelY.add(y + Random.nextGaussian().toFloat() * noiseLevel)
        accelZ.add(z + Random.nextGaussian().toFloat() * noiseLevel)
    }

    return Triple(accelX, accelY, accelZ)
}


