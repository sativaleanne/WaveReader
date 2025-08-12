package com.maciel.wavereader.data

import com.maciel.wavereader.model.WaveApiQuery
import com.maciel.wavereader.model.WaveDataResponse
import com.maciel.wavereader.network.WaveApiService

interface WaveApiRepository {
    //fetch list of data from api
    suspend fun getWaveApiData(query: WaveApiQuery): WaveDataResponse
}

class NetworkWaveApiRepository(
    private val waveApiService: WaveApiService
) : WaveApiRepository {
    override suspend fun getWaveApiData(query: WaveApiQuery): WaveDataResponse {
        val variableList = query.variables.joinToString(",") { it.paramName }
        return waveApiService.getWaveData(
            latitude = query.latitude,
            longitude = query.longitude,
            hourly = variableList,
            current = variableList,
            forecastDays = query.forecastDays,
            lengthUnit = query.lengthUnit
        )
    }
}