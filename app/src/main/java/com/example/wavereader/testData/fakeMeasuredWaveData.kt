package com.example.wavereader.testData

import com.example.wavereader.model.MeasuredWaveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class FakeMeasuredWaveData {
    private val random = Random(System.currentTimeMillis())

    // Generate fake wave data as a Flow that emits every second
    fun generateWaveData(): Flow<MeasuredWaveData> = flow {
        while (true) {
            val waveHeight = random.nextDouble(0.5, 3.0) // Random height between 0.5m - 3m
            val wavePeriod = random.nextDouble(2.0, 10.0) // Random period between 2s - 10s
            val waveDirection = random.nextDouble(0.0, 360.0) // Random direction 0° - 360°

            emit(MeasuredWaveData(waveHeight.toFloat(), wavePeriod.toFloat(), waveDirection.toFloat()))
            delay(1000) // Simulate 1-second interval
        }
    }
}