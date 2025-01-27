package com.example.wavereader.data

import com.example.wavereader.network.WaveApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

//Dependency injection container at app level
interface AppContainer {
    val waveApiRepository: WaveApiRepository
}

class DefaultAppContainer : AppContainer {
    private val baseUrl = "https://marine-api.open-meteo.com/v1/"

    /**
     * Use the Retrofit builder to build a retrofit object using a kotlinx.serialization converter
     */
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        //.addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .build()

    //Retrofit service object for creating api calls
    private val retrofitService: WaveApiService by lazy {
        retrofit.create(WaveApiService::class.java)
    }

    //implementation for wave api repository
    override val waveApiRepository : WaveApiRepository by lazy {
        NetworkWaveApiRepository(retrofitService)
    }

}