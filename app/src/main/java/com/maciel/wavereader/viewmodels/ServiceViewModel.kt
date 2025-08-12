package com.maciel.wavereader.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.maciel.wavereader.WaveReaderApplication
import com.maciel.wavereader.data.WaveApiRepository
import com.maciel.wavereader.model.WaveApiQuery
import com.maciel.wavereader.model.WaveDataResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/*
* Service View Model for controlling the api calls.
 */
class ServiceViewModel(
    private val waveApiRepository: WaveApiRepository
) : ViewModel() {
    var serviceUiState: UiState<WaveDataResponse> by mutableStateOf(UiState.Loading)
        private set

    fun fetchWaveData(query: WaveApiQuery) {
        viewModelScope.launch {
            serviceUiState = UiState.Loading
            serviceUiState = try {
                val data = waveApiRepository.getWaveApiData(query)
                UiState.Success(data)
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