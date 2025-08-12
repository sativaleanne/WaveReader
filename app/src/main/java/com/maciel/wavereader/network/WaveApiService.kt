package com.maciel.wavereader.network

import com.maciel.wavereader.model.WaveDataResponse
import retrofit2.http.GET
import retrofit2.http.Query

//Interface that defines how Retrofit talks to web server using HTTP requests
interface WaveApiService {
    @GET("marine")
    suspend fun getWaveData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String,
        @Query("current") current: String,
        @Query("forecast_days") forecastDays: Int = 1,
        @Query("length_unit") lengthUnit: String = "imperial",
        @Query("timezone") timezone: String = "auto"
    ): WaveDataResponse
}

