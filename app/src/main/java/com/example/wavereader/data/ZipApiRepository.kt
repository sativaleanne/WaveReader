package com.example.wavereader.data

import com.example.wavereader.model.ZipDataResponse
import com.example.wavereader.network.ZipApiService

interface ZipApiRepository {
    //fetch list of data from api
    suspend fun getZipApiData(zipCode: String): ZipDataResponse
}

class NetworkZipApiRepository(
    private val zipApiService: ZipApiService
) : ZipApiRepository {
    private val apiKey = "b5a4e53625ee7a5635576045faa66e35"
    override suspend fun getZipApiData(zipCode: String): ZipDataResponse = zipApiService.getCoordinates("$zipCode,US", apiKey)
}