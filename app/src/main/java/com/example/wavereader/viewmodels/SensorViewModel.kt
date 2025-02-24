package com.example.wavereader.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import com.example.wavereader.model.MeasuredWaveData
import com.example.wavereader.waveCalculator.calculateWaveDirection
import com.example.wavereader.waveCalculator.calculateWaveHeight
import com.example.wavereader.waveCalculator.calculateWavePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WaveUiState(
        val measuredWaveList: List<MeasuredWaveData> = emptyList(),
        val height: Float? = null,
        val period: Float? = null,
        val direction: Float? = null,
)

class SensorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

        private val _uiState = MutableStateFlow(WaveUiState())
        val uiState: StateFlow<WaveUiState> = _uiState.asStateFlow()

        // Get reference to the sensor service
        private val sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Previous timestamp
        private var lastTimestamp: Long = 0L

        private val gravity = FloatArray(3) { 0f }
        private val alpha = 0.8f

        private val horizontalAcceleration = mutableListOf<Array<Float>>()
        private val verticalAcceleration = mutableListOf<Float>()

        private val accelerometerReading = FloatArray(3)
        private val magnetometerReading = FloatArray(3)

        private val rotationMatrix = FloatArray(9)
        private val orientationAngles = FloatArray(3)


        private var samplingRate: Float = 0f
        private var dt: Float = 0f

        private var startTime: Long = 0
        private var startDataBufferTime: Long = 0
        private var dataProcessTime: Long = 0


        private fun updateMeasuredWaveData(height: Float, period: Float, direction: Float, time: Float) {
                _uiState.update { currentState ->
                        val updatedList = currentState.measuredWaveList.toMutableList().apply {
                                add(MeasuredWaveData(height, period, direction, time))
                                if (size > 50) removeAt(0) // Maintain fixed of 50
                        }
                        currentState.copy(
                                measuredWaveList = updatedList,
                                height = height,
                                period = period,
                                direction = direction
                        )
                }
        }

        fun clearMeasuredWaveData() {
                _uiState.update { currentState ->
                        val updatedList = currentState.measuredWaveList.toMutableList().apply {
                                clear()
                        }
                        currentState.copy(
                                measuredWaveList = updatedList,
                                height = null,
                                period = null,
                                direction = null
                        )
                }
                verticalAcceleration.clear()
                horizontalAcceleration.clear()
                lastTimestamp = 0L
        }

        fun startSensors() {
                startTime = SystemClock.elapsedRealtime()
                startDataBufferTime = startTime
                gyroscope?.let {
                        sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                }
                accelerometer?.let {
                        sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                }
                magnetometer?.let {
                        sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                }
        }

        fun stopSensors() {
                sensorManager.unregisterListener(this)
        }

        override fun onSensorChanged(event: SensorEvent) {

                val currentTime = SystemClock.elapsedRealtime()

                updateSamplingRate(event.timestamp)

                when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                                filterData(event.values[0], event.values[1], event.values[2])
                                accelerometerReading
                                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                        }
                        Sensor.TYPE_GYROSCOPE -> {
                                //TODO
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                        }
                }

                // Process data only every 2 seconds
                if (currentTime - dataProcessTime >= 2000L) {
                        processData()
                        dataProcessTime = currentTime
                }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                //
        }

        private fun getMagneticHeading(): Float {
                SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                return (azimuth + 360) % 360 // Normalize to 0-360°
        }

        private fun updateSamplingRate(currentTimestamp: Long) {
                if (lastTimestamp != 0L) {
                        dt = (currentTimestamp - lastTimestamp) / 1_000_000_000f
                        samplingRate = if (dt > 0) 1.0f / dt else 0f
                }
                lastTimestamp = currentTimestamp
        }

        private fun filterData(x: Float, y: Float, z: Float) {

                // Low-pass filter for gravity
                gravity[0] = alpha * gravity[0] + (1 - alpha) * x
                gravity[1] = alpha * gravity[1] + (1 - alpha) * y
                gravity[2] = alpha * gravity[2] + (1 - alpha) * z

                // High-pass filter for linear acceleration
                val linearAcceleration = arrayOf(x - gravity[0], y - gravity[1], z - gravity[2])

                // Use X, Y axis for horizontal movement (Wave Direction)
                horizontalAcceleration.add(arrayOf(linearAcceleration[0], linearAcceleration[1]))
                // Use Z-axis for vertical movement (wave height and period calculations)
                verticalAcceleration.add(linearAcceleration[2])

                // Remove old data to keep buffer size consistent
                if (verticalAcceleration.size > 256) verticalAcceleration.removeAt(0)
                if (horizontalAcceleration.size > 256) horizontalAcceleration.removeAt(0)
        }

        private fun processData() {

                if (verticalAcceleration.size < 50 || horizontalAcceleration.size < 50) return

                val elapsedTime = (SystemClock.elapsedRealtime() - startTime) / 1000f

                val waveHeight = calculateWaveHeight(verticalAcceleration, dt)
                val wavePeriod = calculateWavePeriod(verticalAcceleration, samplingRate)

                val accelX = horizontalAcceleration.map{ it[0] }
                val accelY = horizontalAcceleration.map { it[1] }
                var waveDirection = calculateWaveDirection(accelX, accelY)

                // Align with magnetic north
                waveDirection = (waveDirection + getMagneticHeading()) % 360

                println("Wave Height: $waveHeight")
                println("Wave Period: $wavePeriod")
                println("Wave Direction: $waveDirection")

                updateMeasuredWaveData(waveHeight, wavePeriod, waveDirection, elapsedTime)

        }
}