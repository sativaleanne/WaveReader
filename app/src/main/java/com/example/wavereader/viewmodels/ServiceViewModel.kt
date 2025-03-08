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

sealed interface ServiceUiState {
    data class Success(val waveData: WaveDataResponse) : ServiceUiState
    object Error : ServiceUiState
    object Loading : ServiceUiState
}

class ServiceViewModel(
    private val waveApiRepository: WaveApiRepository,
    private val locationViewModel: LocationViewModel
) : ViewModel() {
    var serviceUiState: ServiceUiState by mutableStateOf(ServiceUiState.Loading)
        private set

    init {
        observeLocationUpdates()
    }

    private fun observeLocationUpdates() {
        locationViewModel.coordinatesState.observeForever { coordinates ->
            val (lat, lon) = coordinates
            if (locationViewModel.zipCode.isNotEmpty()) {
                fetchWaveData(lat, lon)
            }
        }
    }


    fun fetchWaveData(lat: Double, long: Double) {
        viewModelScope.launch {
            serviceUiState = ServiceUiState.Loading
            serviceUiState = try {
                val dataResult = waveApiRepository.getWaveApiData(lat, long)
                ServiceUiState.Success(waveData = dataResult)
            } catch (e: IOException) {
                ServiceUiState.Error
            } catch (e: HttpException) {
                ServiceUiState.Error
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WaveReaderApplication)
                val waveApiRepository = application.container.waveApiRepository
                val locationViewModel = application.container.locationViewModel
                ServiceViewModel(waveApiRepository, locationViewModel)
            }
        }
    }
}