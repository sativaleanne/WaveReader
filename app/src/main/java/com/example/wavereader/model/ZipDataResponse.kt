package com.example.wavereader.model

import kotlinx.serialization.Serializable

@Serializable
data class ZipDataResponse(
    val zip: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String
)