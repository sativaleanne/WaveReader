package com.example.wavereader.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.wavereader.WaveReaderApplication
import com.example.wavereader.data.ZipApiRepository
import kotlinx.coroutines.launch

class LocationViewModel(private val zipApiRepository: ZipApiRepository) : ViewModel() {

    private val _coordinatesState = MutableLiveData(Pair(0.0, 0.0))
    val coordinatesState: LiveData<Pair<Double, Double>> = _coordinatesState

    var locationError: Boolean by mutableStateOf(false)
        private set

    fun updateCoordinates(lat: Double, lon: Double) {
        _coordinatesState.postValue(Pair(lat, lon))
    }

    fun fetchCoordinates(zipCode: String) {
        viewModelScope.launch {
            try {
                val response = zipApiRepository.getZipApiData(zipCode)
                updateCoordinates(response.lat, response.lon)
                locationError = false
            } catch (e: Exception) {
                locationError = true
                e.printStackTrace()
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WaveReaderApplication
                val zipApiRepository = application.container.zipApiRepository
                LocationViewModel(zipApiRepository)
            }
        }
    }
}