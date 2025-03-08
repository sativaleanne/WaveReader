package com.example.wavereader.model

data class HistoryRecord(
    val id: String,
    val timestamp: String,
    val location: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val dataPoints: List<WaveDataPoint>
)

data class WaveDataPoint(
    val time: Float,
    val height: Float,
    val period: Float,
    val direction: Float
)
