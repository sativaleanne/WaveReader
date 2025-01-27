package com.example.wavereader.data

import com.example.wavereader.model.WaveDataResponse
import com.example.wavereader.network.WaveApiService

interface WaveApiRepository {
    //fetch list of data from api
    suspend fun getWaveApiData(): WaveDataResponse
}

class NetworkWaveApiRepository(
    private val waveApiService: WaveApiService
) : WaveApiRepository {
    override suspend fun getWaveApiData(): WaveDataResponse = waveApiService.getWaveData()
}