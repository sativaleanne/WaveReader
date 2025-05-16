package com.example.wavereader.utils

import kotlin.math.pow

fun movingAverage(data: List<Float>, window: Int): List<Float> {
    if (data.size < window || window < 1) return emptyList()
    return data.windowed(window, 1) { it.average().toFloat() }
}

fun trendDirection(data: List<Float>, threshold: Float = 0.1f): String {
    if (data.size < 2) return "N/A"
    val change = data.last() - data.first()
    return when {
        change > threshold -> "Increasing"
        change < -threshold -> "Decreasing"
        else -> "Stable"
    }
}

fun slope(data: List<Float>): Float {
    if (data.size < 2) return 0f
    val n = data.size
    val x = (0 until n).map { it.toFloat() }
    val xMean = x.average().toFloat()
    val yMean = data.average().toFloat()

    val numerator = x.zip(data).sumOf { (xi, yi) -> ((xi - xMean) * (yi - yMean)).toDouble() }
    val denominator = x.sumOf { xi -> ((xi - xMean).pow(2)).toDouble() }

    return if (denominator == 0.0) 0f else (numerator / denominator).toFloat()
}

fun forecastNext(data: List<Float>): Float? {
    if (data.size < 2) return null
    val m = slope(data)
    val x = (0 until data.size).map { it.toFloat() }
    val xMean = x.average().toFloat()
    val yMean = data.average().toFloat()
    val b = yMean - m * xMean
    return m * data.size + b
}

fun predictNextBigWave() {
    //TODO
}