package com.example.wavereader.network

import com.example.wavereader.model.ZipDataResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ZipApiService {
    @GET("geo/1.0/zip")
    suspend fun getCoordinates(
        @Query("zip") zip: String,
        @Query("appid") apiKey: String
    ): ZipDataResponse
}