package com.maciel.wavereader.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.maciel.wavereader.network.WaveApiService
import com.maciel.wavereader.viewmodels.LocationViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

//Dependency injection container at app level
interface AppContainer {
    val waveApiRepository: WaveApiRepository
    val locationViewModel: LocationViewModel
    val firestoreRepository: FirestoreRepository
}

class DefaultAppContainer : AppContainer {
    private val waveBaseUrl = "https://marine-api.open-meteo.com/v1/"

    /**
     * Use the Retrofit builder to build a retrofit object using a kotlinx.serialization converter
     * Retrofit for Wave API
     */
    private val retrofitWave: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(waveBaseUrl)
        .build()

    //Retrofit service object for creating api calls
    private val retrofitWaveService: WaveApiService by lazy {
        retrofitWave.create(WaveApiService::class.java)
    }

    //implementation for wave api repository
    override val waveApiRepository : WaveApiRepository by lazy {
        NetworkWaveApiRepository(retrofitWaveService)
    }

    override val locationViewModel by lazy {
        LocationViewModel()
    }

    override val firestoreRepository: FirestoreRepository by lazy {
        FirestoreRepository()
    }

}