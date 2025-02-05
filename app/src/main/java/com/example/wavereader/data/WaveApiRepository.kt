package com.example.wavereader.data

import com.example.wavereader.model.WaveDataResponse
import com.example.wavereader.network.WaveApiService

interface WaveApiRepository {
    //fetch list of data from api
    suspend fun getWaveApiData(lat: Double, long: Double): WaveDataResponse
}

class NetworkWaveApiRepository(
    private val waveApiService: WaveApiService
) : WaveApiRepository {
    override suspend fun getWaveApiData(lat: Double, long: Double): WaveDataResponse = waveApiService.getWaveData(lat, long)
}