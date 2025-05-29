package com.example.wavereader.testData

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavereader.model.MeasuredWaveData
import com.example.wavereader.utils.hanningWindow
import com.example.wavereader.utils.calculateSpectralMoments
import com.example.wavereader.utils.calculateWaveDirection
import com.example.wavereader.utils.computeSpectralDensity
import com.example.wavereader.utils.computeWaveMetricsFromSpectrum
import com.example.wavereader.utils.getFft
import com.example.wavereader.utils.nextBigWaveConfidence
import com.example.wavereader.viewmodels.WaveUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FakeSensorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WaveUiState())
    val uiState: StateFlow<WaveUiState> = _uiState.asStateFlow()

    private val _bigWaveConfidence = MutableStateFlow(0f)
    val bigWaveConfidence: StateFlow<Float> = _bigWaveConfidence.asStateFlow()

    private var isRunning = false

    fun startSimulation() {
        if (isRunning) return
        isRunning = true
        viewModelScope.launch {
            var time = 0f
            while (isRunning) {
                // big wave every 20 seconds
                val bigWave = (time.toInt() / 5) % 4 == 3 // every 20s
                val amplitude = if (bigWave) 2f else 1f

                val (accelX, accelY, accelZ) = rawAccelerationData(
                    freq = 0.2f,
                    phaseOffset = 60f,
                    duration = 1f,
                    sampleRate = 50f,
                    amplitude = amplitude,
                    noiseLevel = 0.02f
                )

                val window = hanningWindow(accelZ)
                val fft = getFft(window, window.size)
                val spectrum = computeSpectralDensity(fft, window.size)
                val (m0, m1, m2) = calculateSpectralMoments(spectrum, 50f)
                val (height, period, _) = computeWaveMetricsFromSpectrum(m0, m1, m2)
                val direction = calculateWaveDirection(accelX, accelY)

                _uiState.update {
                    val updated = it.measuredWaveList.toMutableList().apply {
                        add(MeasuredWaveData(height, period, direction, time))
                        if (size > 50) removeAt(0)
                    }
                    it.copy(measuredWaveList = updated, height = height, period = period, direction = direction)
                }
                val recent = _uiState.value.measuredWaveList
                val confidence = nextBigWaveConfidence(recent)
                _bigWaveConfidence.value = confidence

                time += 1f
                delay(1000L) // 1 second interval
            }
        }
    }

    fun stopSimulation() {
        isRunning = false
    }

    fun clearSimulation() {
        _uiState.value = WaveUiState()
        _bigWaveConfidence.value = 0f
    }
}
