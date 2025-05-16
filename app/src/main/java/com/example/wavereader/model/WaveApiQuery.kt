package com.example.wavereader.model

data class WaveApiQuery(
    val latitude: Double,
    val longitude: Double,
    val variables: Set<ApiVariable> = setOf(ApiVariable.WaveHeight, ApiVariable.WavePeriod, ApiVariable.WaveDirection),
    val forecastDays: Int = 1,
    val lengthUnit: String = "imperial"
)

enum class ApiVariable(val label: String, val paramName: String) {
    WaveHeight("Wave Height", "wave_height"),
    WaveDirection("Wave Direction", "wave_direction"),
    WavePeriod("Wave Period", "wave_period"),
    WindWaveHeight("Wind Wave Height", "wind_wave_height"),
    WindWaveDirection("Wind Wave Direction", "wind_wave_direction"),
    WindWavePeriod("Wind Wave Period", "wind_wave_period"),
    SwellWaveHeight("Swell Wave Height", "swell_wave_height"),
    SwellWaveDirection("Swell Wave Direction", "swell_wave_direction"),
    SwellWavePeriod("Swell Wave Period", "swell_wave_period")
}