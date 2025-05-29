package com.example.wavereader.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import com.example.wavereader.data.RecordSessionRepository
import com.example.wavereader.model.MeasuredWaveData
import com.example.wavereader.utils.hanningWindow
import com.example.wavereader.utils.calculateSpectralMoments
import com.example.wavereader.utils.calculateWaveDirection
import com.example.wavereader.utils.computeSpectralDensity
import com.example.wavereader.utils.computeWaveMetricsFromSpectrum
import com.example.wavereader.utils.getFft
import com.example.wavereader.utils.nextBigWaveConfidence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

class SensorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

        private val _uiState = MutableStateFlow(WaveUiState())
        val uiState: StateFlow<WaveUiState> = _uiState.asStateFlow()

        private val _bigWaveConfidence = MutableStateFlow(0f)
        val bigWaveConfidence: StateFlow<Float> = _bigWaveConfidence.asStateFlow()

        private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        private val recordSessionRepository = RecordSessionRepository()

        private var accelerometer: Sensor? = null
        private var gyroscope: Sensor? = null
        private var magnetometer: Sensor? = null

        private var lastTimestamp: Long = 0L
        private var dt: Float = 0f
        private var samplingRate: Float = 50f
        private val alpha = 0.8f

        private val gravity = FloatArray(3)
        private val accelerometerReading = FloatArray(3)
        private val magnetometerReading = FloatArray(3)
        private val rotationMatrix = FloatArray(9)
        private val orientationAngles = FloatArray(3)

        private val verticalAcceleration = mutableListOf<Float>()
        private val horizontalAcceleration = mutableListOf<Array<Float>>()
        private var filteredWaveDirection: Float? = null

        private var startTime = 0L
        private var dataProcessTime = 0L
        private var currentLocationName: String = "Unknown location"
        private var currentLatLng: Pair<Double, Double>? = null

        override fun onSensorChanged(event: SensorEvent) {
                val currentTime = SystemClock.elapsedRealtime()
                updateSamplingRate(event.timestamp)

                when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                                filterData(event.values[0], event.values[1], event.values[2])
                                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                        }
                        Sensor.TYPE_GYROSCOPE -> {
                                val gyroscopeDt = if (lastTimestamp != 0L) (event.timestamp - lastTimestamp) / 1_000_000_000f else 0f
                                val gyroZ = event.values[2]

                                val sensorHeading = getMagneticHeading()
                                filteredWaveDirection = if (filteredWaveDirection == null) {
                                        sensorHeading
                                } else {
                                        val integratedAngle = filteredWaveDirection!! + Math.toDegrees(gyroZ * gyroscopeDt.toDouble()).toFloat()
                                        alpha * integratedAngle + (1 - alpha) * sensorHeading
                                }
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                        }
                }

                if (currentTime - dataProcessTime >= 2000L) {
                        processData()
                        dataProcessTime = currentTime
                }
        }

        // Data processing
        private fun processData() {
                if (verticalAcceleration.size < 1024) return

                val windowSize = 1024
                val stepSize = 512

                // Overlap data chunks
                val segments = overlapData(verticalAcceleration, windowSize, stepSize)

                val getHeights = mutableListOf<Float>()
                val getPeriods = mutableListOf<Float>()

                for (segment in segments) {
                        // Windowing to reduce spectral leakage
                        val window = hanningWindow(segment)
                        // FastFourier Transform
                        val fft = getFft(window, window.size)
                        // Convert FFT to spectral density
                        val spectrum = computeSpectralDensity(fft, window.size)

                        // Extract spectral moments: m0, m1, m2
                        val (m0, m1, m2) = calculateSpectralMoments(spectrum, samplingRate)
                        // Compute significant wave height, average period, zero-crossing period
                        // TODO: Update needed variables
                        val (sigWaveHeight, avePeriod, tZero) = computeWaveMetricsFromSpectrum(m0, m1, m2)

                        getHeights.add(sigWaveHeight)
                        getPeriods.add(avePeriod)
                }
                // Final average values
                val height = getHeights.average().toFloat()
                val period = getPeriods.average().toFloat()

                val elapsedTime = (SystemClock.elapsedRealtime() - startTime) / 1000f
                val accelX = horizontalAcceleration.map { it[0] }
                val accelY = horizontalAcceleration.map { it[1] }
                val fftDirection = calculateWaveDirection(accelX, accelY)
                val direction = filteredWaveDirection?.let {
                        (it + fftDirection) / 2f
                } ?: fftDirection

                updateMeasuredWaveData(height, period, direction, elapsedTime)
                val recent = _uiState.value.measuredWaveList
                val confidence = nextBigWaveConfidence(recent)
                _bigWaveConfidence.value = confidence
        }

        private fun updateMeasuredWaveData(height: Float, period: Float, direction: Float, time: Float) {
                _uiState.update { state ->
                        val updated = state.measuredWaveList.toMutableList().apply {
                                add(MeasuredWaveData(height, period, direction, time))
                                if (size > 50) removeAt(0)
                        }
                        state.copy(measuredWaveList = updated, height = height, period = period, direction = direction)
                }
        }

        private fun getMagneticHeading(): Float {
                SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                return (azimuth + 360) % 360
        }

        // Get Sampling Rate
        private fun updateSamplingRate(currentTimestamp: Long) {
                if (lastTimestamp != 0L) {
                        dt = (currentTimestamp - lastTimestamp) / 1_000_000_000f
                        samplingRate = if (dt > 0) 1f / dt else 0f
                }
                lastTimestamp = currentTimestamp
        }

        // Remove Gravity and filter to correct location and size
        private fun filterData(x: Float, y: Float, z: Float) {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * x
                gravity[1] = alpha * gravity[1] + (1 - alpha) * y
                gravity[2] = alpha * gravity[2] + (1 - alpha) * z

                val linear = arrayOf(x - gravity[0], y - gravity[1], z - gravity[2])
                horizontalAcceleration.add(arrayOf(linear[0], linear[1]))
                verticalAcceleration.add(linear[2])

                if (verticalAcceleration.size > 2048) verticalAcceleration.removeAt(0)
                if (horizontalAcceleration.size > 2048) horizontalAcceleration.removeAt(0)
        }

        // Divide data into overlapping chunks
        private fun overlapData(data: List<Float>, windowSize: Int, stepSize: Int): List<List<Float>> {
                val segments = mutableListOf<List<Float>>()
                var i = 0
                while (i + windowSize <= data.size) {
                        segments.add(data.subList(i, i + windowSize))
                        i += stepSize
                }
                return segments
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        fun startSensors() {
                startTime = SystemClock.elapsedRealtime()
                gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
                accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
                magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }

        fun stopSensors() { sensorManager.unregisterListener(this) }

        // Make sure device has all sensor hardware
        fun checkSensors(): Boolean {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                return accelerometer != null && gyroscope != null && magnetometer != null
        }

        // Set Location for Saving
        fun setCurrentLocation(name: String, latLng: Pair<Double, Double>?) {
                currentLocationName = name
                currentLatLng = latLng
        }

        // On Clear Button Click
        fun clearMeasuredWaveData() {
                _uiState.update {
                        it.copy(measuredWaveList = emptyList(), height = null, period = null, direction = null)
                }
                verticalAcceleration.clear()
                horizontalAcceleration.clear()
                _bigWaveConfidence.value = 0f
                lastTimestamp = 0L
        }

        // On Save Button Click
        fun saveToFirestore() {
                recordSessionRepository.saveSession(
                        measuredData = uiState.value.measuredWaveList,
                        locationName = currentLocationName,
                        latLng = currentLatLng,
                        onSuccess = { println("Wave session saved successfully!") },
                        onFailure = { e -> println("Error saving wave session: $e") }
                )
        }
}


