package com.example.wavereader.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.wavereader.WaveReaderApplication
import com.example.wavereader.data.WaveApiRepository
import com.example.wavereader.model.WaveDataResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/*
* Service View Model for controlling the api calls.
* TODO: Add filters to getWaveApidata
 */
class ServiceViewModel(
    private val waveApiRepository: WaveApiRepository
) : ViewModel() {
    var serviceUiState: UiState<WaveDataResponse> by mutableStateOf(UiState.Loading)
        private set

    fun fetchWaveData(coordinates: Pair<Double, Double>) {
        fetchWaveData(coordinates.first, coordinates.second)
    }

    fun fetchWaveData(lat: Double, long: Double) {
        viewModelScope.launch {
            serviceUiState = UiState.Loading
            serviceUiState = try {
                val dataResult = waveApiRepository.getWaveApiData(lat, long)
                UiState.Success(dataResult)
            } catch (e: IOException) {
                UiState.Error("Network error")
            } catch (e: HttpException) {
                UiState.Error("Server error")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WaveReaderApplication)
                val waveApiRepository = application.container.waveApiRepository
                val locationViewModel = application.container.locationViewModel
                ServiceViewModel(waveApiRepository)
            }
        }
    }
}