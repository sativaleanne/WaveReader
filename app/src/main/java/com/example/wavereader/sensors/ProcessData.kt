package com.example.wavereader.sensors

import android.app.Activity
import com.example.wavereader.WaveViewModel


    val accelerationData = mutableListOf<Float>()
    val samplingRate = 0.02f // Example: 50 Hz (1/50 seconds)

    fun processAccelerometerData(x: Float, y: Float, z: Float) {
        // Filter and collect Z-axis data
        val filteredZ = z - 9.8f
        accelerationData.add(filteredZ)

        if (accelerationData.size > 100) { // Process data after collecting enough samples
            val displacement = calculateDisplacement(accelerationData, samplingRate)
            val frequency = calculateFrequency(accelerationData, 1 / samplingRate)
            println("Wave Height: ${displacement.maxOrNull()}")
            println("Wave Frequency: $frequency")

            // Clear data for the next batch
            accelerationData.clear()
        }
    }


    // Previous timestamp to calculate time intervals
    var lastTimestamp: Long? = null

    fun processGyroscopeData(
        rotationX: Float,
        rotationY: Float,
        rotationZ: Float,
        timestamp: Long
    ) {
        // Calculate the time difference (dt) in seconds
        val dt = lastTimestamp?.let { (timestamp - it) / 1_000_000_000.0f } ?: 0f
        lastTimestamp = timestamp

        if (dt > 0) {
            // Pass raw angular velocities and time interval to the tilt calculation function
            val tilt = calculateTilt(rotationX, rotationY, rotationZ, dt)

            // Log or use the calculated tilt values
            println("Tilt X: ${tilt.first}°")
            println("Tilt Y: ${tilt.second}°")
            println("Tilt Z: ${tilt.third}°")

        }
    }