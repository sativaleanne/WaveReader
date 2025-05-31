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
import com.example.wavereader.utils.estimateZeroCrossingPeriod
import com.example.wavereader.utils.getFft
import com.example.wavereader.utils.highPassFilter
import com.example.wavereader.utils.medianFilter
import com.example.wavereader.utils.nextBigWaveConfidence
import com.example.wavereader.utils.movingAverage
import com.example.wavereader.utils.smoothOutput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.sqrt

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
        private var previousFilteredDirection: Float? = null

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
                                if (filteredWaveDirection == null) {
                                        filteredWaveDirection = sensorHeading
                                } else {
                                        val integratedAngle = filteredWaveDirection!! + Math.toDegrees(gyroZ * gyroscopeDt.toDouble()).toFloat()
                                        val blended = alpha * integratedAngle + (1 - alpha) * sensorHeading
                                        filteredWaveDirection = (blended + 360) % 360  // Clamp to [0, 360)
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

                // High-pass filter removes tilt and drift
                val highPassed = highPassFilter(verticalAcceleration, 51)

                // Segment data into overlapping windows
                val segments = overlapData(highPassed, windowSize, stepSize)

                val heights = mutableListOf<Float>()
                val periods = mutableListOf<Float>()

                for (segment in segments) {
                        // Reject extremely high or low windows
                        val rms = sqrt(segment.sumOf { it.toDouble() * it.toDouble() } / segment.size)
                        if (rms < 0.01f || rms > 10f) continue

                        // Median filter to remove noise
                        val medianed = medianFilter(segment, 5)

                        // Smoothing
                        val smoothed = movingAverage(medianed, 5)

                        // Windowing
                        val windowed = hanningWindow(smoothed)

                        // Compute frequency-domain features
                        val fft = getFft(windowed, windowed.size)
                        val spectrum = computeSpectralDensity(fft, windowed.size)
                        val (m0, m1, m2) = calculateSpectralMoments(spectrum, samplingRate)

                        // Get spectral and zero-crossing wave periods
                        val (sigWaveHeight, avePeriod, _) = computeWaveMetricsFromSpectrum(m0, m1, m2)
                        val zeroCrossPeriod = estimateZeroCrossingPeriod(segment, samplingRate)

                        // stability
                        val blendedPeriod = if (zeroCrossPeriod.isFinite()) {
                                (avePeriod + zeroCrossPeriod) / 2f
                        } else avePeriod

                        // only valid stuff
                        if (sigWaveHeight.isFinite() && blendedPeriod.isFinite()) {
                                heights.add(sigWaveHeight)
                                periods.add(blendedPeriod)
                        }
                }

                if (heights.isEmpty() || periods.isEmpty()) return

                // Smoothing
                val avgHeight = heights.average().toFloat()
                val avgPeriod = periods.average().toFloat()
                val smoothedHeight = smoothOutput(_uiState.value.height, avgHeight)
                val smoothedPeriod = smoothOutput(_uiState.value.period, avgPeriod)

                // wave direction using horizontal motion and gyroscope
                val elapsedTime = (SystemClock.elapsedRealtime() - startTime) / 1000f
                val accelX = horizontalAcceleration.map { it[0] }
                val accelY = horizontalAcceleration.map { it[1] }
                val rawFftDirection = calculateWaveDirection(accelX, accelY)
                val fftDirection = previousFilteredDirection?.let {
                        val delta = kotlin.math.abs(rawFftDirection - it)
                        if (delta < 45f) smoothOutput(it, rawFftDirection, alpha = 0.8f) else it
                } ?: rawFftDirection
                previousFilteredDirection = fftDirection

                val direction = filteredWaveDirection?.let {
                        (it + fftDirection) / 2f
                } ?: fftDirection

                // Update and check next big wave
                updateMeasuredWaveData(smoothedHeight, smoothedPeriod, direction, elapsedTime)
                _bigWaveConfidence.value = nextBigWaveConfidence(_uiState.value.measuredWaveList)
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
                // Gravity filtering
                gravity[0] = alpha * gravity[0] + (1 - alpha) * x
                gravity[1] = alpha * gravity[1] + (1 - alpha) * y
                gravity[2] = alpha * gravity[2] + (1 - alpha) * z

                val linearAcc = floatArrayOf(
                        x - gravity[0],
                        y - gravity[1],
                        z - gravity[2]
                )

                // Use rotation matrix to remap linear acceleration to earth coordinates
                val earthAcc = FloatArray(3)

                if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
                        // Apply rotation matrix to get earth-relative acceleration
                        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, rotationMatrix)

                        // Matrix multiplication: rotationMatrix * linearAcc
                        earthAcc[0] = rotationMatrix[0] * linearAcc[0] + rotationMatrix[1] * linearAcc[1] + rotationMatrix[2] * linearAcc[2]
                        earthAcc[1] = rotationMatrix[3] * linearAcc[0] + rotationMatrix[4] * linearAcc[1] + rotationMatrix[5] * linearAcc[2]
                        earthAcc[2] = rotationMatrix[6] * linearAcc[0] + rotationMatrix[7] * linearAcc[1] + rotationMatrix[8] * linearAcc[2]
                } else {
                        // fallback if rotation matrix not available
                        earthAcc[0] = linearAcc[0]
                        earthAcc[1] = linearAcc[1]
                        earthAcc[2] = linearAcc[2]
                }

                // Add to buffers
                horizontalAcceleration.add(arrayOf(earthAcc[0], earthAcc[1]))
                verticalAcceleration.add(earthAcc[2]) // up

                // Trim buffer size
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
                gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
                accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
                magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
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


