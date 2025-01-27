package com.example.wavereader.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WaveDataResponse (
    val latitude: Float,
    val longitude: Float,

    @SerialName("generationtime_ms")
    val generationtimeMS: Double,

    @SerialName("utc_offset_seconds")
    val utcOffsetSeconds: Long,

    val timezone: String,

    @SerialName("timezone_abbreviation")
    val timezoneAbbreviation: String,

    val elevation: Float,

    @SerialName("current_units")
    val currentUnits: Units,

    @SerialName("current")
    val current: Current,

    @SerialName("hourly_units")
    val hourlyUnits: Units,

    @SerialName("hourly")
    val hourly: Hourly
)

@Serializable
data class Current (
    val time: String,
    val interval: Long,

    @SerialName("wave_height")
    val waveHeight: Float,

    @SerialName("wave_direction")
    val waveDirection: Float,

    @SerialName("wave_period")
    val wavePeriod: Float
)

@Serializable
data class Units (
    val time: String,
    val interval: String? = null,

    @SerialName("wave_height")
    val waveHeight: String,

    @SerialName("wave_direction")
    val waveDirection: String,

    @SerialName("wave_period")
    val wavePeriod: String
)

@Serializable
data class Hourly (
    val time: List<String>,

    @SerialName("wave_height")
    val waveHeight: List<Float>,

    @SerialName("wave_direction")
    val waveDirection: List<Float>,

    @SerialName("wave_period")
    val wavePeriod: List<Float>
)

