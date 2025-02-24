package com.example.wavereader.network

import com.example.wavereader.model.WaveDataResponse
import retrofit2.http.GET
import retrofit2.http.Query

//Interface that defines how Retrofit talks to web server using HTTP requests
interface WaveApiService {
    @GET("marine")
    suspend fun getWaveData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "wave_height,wave_direction,wave_period",
        @Query("hourly") hourly: String = "wave_height,wave_direction,wave_period",
        @Query("length_unit") lengthUnit: String = "imperial",
        @Query("wind_speed_unit") windSpeedUnit: String = "mph",
        @Query("forecast_days") forecastDays: Int = 1
    ): WaveDataResponse
}

