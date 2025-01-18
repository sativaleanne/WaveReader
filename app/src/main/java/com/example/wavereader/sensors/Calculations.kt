package com.example.wavereader.sensors


// Wave height calculation

fun calculateDisplacement(acceleration: List<Float>, dt: Float): List<Float> {
    val velocity = mutableListOf<Float>()
    val displacement = mutableListOf<Float>()
    var currentVelocity = 0f
    var currentDisplacement = 0f
    // Remove gravity? a(filtered) = a[z] - 9.8m/s^2
    for (a in acceleration) {
        currentVelocity += a * dt // acceleration to velocity
        velocity.add(currentVelocity)
        currentDisplacement += currentVelocity * dt // velocity to displacement
        displacement.add(currentDisplacement)
    }
    return displacement
    // Wave height approximately twice the max displacement? H = 2 x max(h)
}

// Frequency calculation

fun calculateFrequency(acceleration: List<Float>, samplingRate: Float): Float {
    val peaks = mutableListOf<Int>()
    for (i in 1 until acceleration.size - 1) {
        if (acceleration[i - 1] < acceleration[i] && acceleration[i] > acceleration[i + 1]) {
            peaks.add(i)
        }
    }
    if (peaks.size < 2) return 0f
    val timeBetweenPeaks = (peaks.last() - peaks.first()) / samplingRate
    val waveFrequency = (peaks.size - 1) / timeBetweenPeaks
    return waveFrequency
}

// Direction calculation
// Variables to store cumulative tilt values
var cumulativeTiltX = 0f
var cumulativeTiltY = 0f
var cumulativeTiltZ = 0f

fun calculateTilt(rotationX: Float, rotationY: Float, rotationZ: Float, dt: Float): Triple<Float, Float, Float> {
    // Integrate angular velocity to compute tilt (in radians)
    cumulativeTiltX += rotationX * dt
    cumulativeTiltY += rotationY * dt
    cumulativeTiltZ += rotationZ * dt

    // Convert tilt from radians to degrees
    val tiltXDegrees = Math.toDegrees(cumulativeTiltX.toDouble()).toFloat()
    val tiltYDegrees = Math.toDegrees(cumulativeTiltY.toDouble()).toFloat()
    val tiltZDegrees = Math.toDegrees(cumulativeTiltZ.toDouble()).toFloat()

    // Return the tilt values as a Triple
    return Triple(tiltXDegrees, tiltYDegrees, tiltZDegrees)
}