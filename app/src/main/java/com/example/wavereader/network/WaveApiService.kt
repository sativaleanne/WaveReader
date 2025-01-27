package com.example.wavereader.network

import com.example.wavereader.model.WaveDataResponse
import retrofit2.http.GET

//Interface that defines how Retrofit talks to web server using HTTP requests
interface WaveApiService {
    @GET("marine?latitude=44.9582&longitude=-124.0179&current=wave_height,wave_direction,wave_period&hourly=wave_height,wave_direction,wave_period&length_unit=imperial&wind_speed_unit=mph&forecast_days=1")
    suspend fun getWaveData(): WaveDataResponse
}

