package com.maciel.wavereader.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maciel.wavereader.data.FirestoreRepository
import com.maciel.wavereader.model.MeasuredWaveData
import com.maciel.wavereader.processing.SensorDataSource
import com.maciel.wavereader.processing.WaveDataProcessor
import com.maciel.wavereader.utils.nextBigWaveConfidence
import com.maciel.wavereader.utils.smoothOutput
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis

/**
* Sensor View Model for control the sensor manager and processing data
* Resources: For calculations and data processing
 * https://www.ndbc.noaa.gov/faq/wavecalc.shtml
 * https://www.ndbc.noaa.gov/wavemeas.pdf
 */
data class WaveUiState(
        val measuredWaveList: List<MeasuredWaveData> = emptyList(),
        val height: Float? = null,
        val period: Float? = null,
        val direction: Float? = null,
)

class SensorViewModel(
    application: Application,
    private val sensorDataSource: SensorDataSource,
    private val firestoreRepository: FirestoreRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WaveUiState())
    val uiState: StateFlow<WaveUiState> = _uiState.asStateFlow()

    private val _bigWaveConfidence = MutableStateFlow(0f)
    val bigWaveConfidence: StateFlow<Float> = _bigWaveConfidence.asStateFlow()

    private val waveDataProcessor = WaveDataProcessor()

    private var currentLocationName: String = "Unknown location"
    private var currentLatLng: Pair<Double, Double>? = null

    private var startTime = 0L
    private var processingJob: Job? = null

    private var smoothedHeight: Float? = null
    private var smoothedPeriod: Float? = null

    /**
     * Check if sensors are available on this device
     */
    fun checkSensors(): Boolean {
        return sensorDataSource.areSensorsAvailable()
    }

    /**
     * Start collecting sensor data
     */
    fun startSensors() {
        startTime = currentTimeMillis()

        // Start platform-specific sensor collection
        sensorDataSource.startListening { sensorData ->
            // Update sampling rate
            waveDataProcessor.updateSamplingRate(sensorData.samplingRate)

            // Add acceleration data to processor
            waveDataProcessor.addAccelerationData(
                vertical = sensorData.verticalAcceleration,
                horizontalX = sensorData.horizontalX,
                horizontalY = sensorData.horizontalY
            )
        }

        // Start periodic processing using coroutine
        processingJob = viewModelScope.launch {
            while (isActive) { // Loop while coroutine is active
                delay(2000L) // Wait 2 seconds
                processData()
            }
        }

    }

    /**
     * Stop collecting sensor data
     */
    fun stopSensors() {
        sensorDataSource.stopListening()
        processingJob?.cancel()
        processingJob = null
    }

    /**
     * Process accumulated sensor data
     */
    private fun processData() {
        val gyroDirection = sensorDataSource.getCurrentGyroDirection()

        val result = waveDataProcessor.processWaveData(gyroDirection) ?: return

        val (avgHeight, avgPeriod, direction) = result

        // Smooth the output
        smoothedHeight = smoothOutput(smoothedHeight, avgHeight)
        smoothedPeriod = smoothOutput(smoothedPeriod, avgPeriod)

        val elapsedTime = (currentTimeMillis() - startTime) / 1000f

        // Update UI state
        updateMeasuredWaveData(
            smoothedHeight ?: avgHeight,
            smoothedPeriod ?: avgPeriod,
            direction,
            elapsedTime
        )

        // Update big wave confidence
        _bigWaveConfidence.value = nextBigWaveConfidence(_uiState.value.measuredWaveList)
    }

    /**
     * Update measured wave data list
     */
    private fun updateMeasuredWaveData(
        height: Float,
        period: Float,
        direction: Float,
        time: Float
    ) {
        _uiState.update { state ->
            val updated = state.measuredWaveList.toMutableList().apply {
                add(MeasuredWaveData(height, period, direction, time))
                if (size > 50) removeAt(0)
            }
            state.copy(
                measuredWaveList = updated,
                height = height,
                period = period,
                direction = direction
            )
        }
    }

    /**
     * Set current location for saving
     */
    fun setCurrentLocation(name: String, latLng: Pair<Double, Double>?) {
        currentLocationName = name
        currentLatLng = latLng
    }

    /**
     * Clear all measured wave data
     */
    fun clearMeasuredWaveData() {
        _uiState.update {
            it.copy(measuredWaveList = emptyList(), height = null, period = null, direction = null)
        }
        waveDataProcessor.clear()
        _bigWaveConfidence.value = 0f
        smoothedHeight = null
        smoothedPeriod = null
    }

    // On Save Button Click
    fun saveToFirestore() {
        firestoreRepository.saveSession(
            measuredData = uiState.value.measuredWaveList,
            locationName = currentLocationName,
            latLng = currentLatLng,
            onSuccess = { println("Wave session saved successfully!") },
            onFailure = { e -> println("Error saving wave session: $e") }
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }


    companion object {
        fun provideFactory(
            application: Application,
            sensorDataSource: SensorDataSource,
            firestoreRepository: FirestoreRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SensorViewModel::class.java)) {
                    return SensorViewModel(application, sensorDataSource, firestoreRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}


