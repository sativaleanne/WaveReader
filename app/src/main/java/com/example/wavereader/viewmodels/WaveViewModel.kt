package com.example.wavereader.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import com.example.wavereader.sensors.calculateDisplacement
import com.example.wavereader.sensors.calculateFrequency
import com.example.wavereader.sensors.calculateTilt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WaveUiState(
        val height: Float? = null,
        val frequency: Float? = null,
        val tiltX: Float? = null,
        val tiltY: Float? = null,
        val tiltZ: Float? = null,


)

class WaveViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
        private val _uiState = MutableStateFlow(WaveUiState())
        val uiState: StateFlow<WaveUiState> = _uiState.asStateFlow()

        // Get reference to the sensor service
        private val sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)



        fun updateTilt(tilt: Triple<Float, Float, Float>) {
                _uiState.update { currentState ->
                        currentState.copy(
                                tiltX = tilt.first,
                                tiltY = tilt.second,
                                tiltZ = tilt.third
                        )
                }
        }

        fun updateHeight(height: Float) {
                _uiState.update { currentState ->
                        currentState.copy(
                                height = height
                        )
                }
        }

        fun updateFrequency(frequency: Float) {
                _uiState.update { currentState ->
                        currentState.copy(
                                frequency = frequency
                        )
                }
        }

        fun startSensors() {
                gyroscope?.let {
                        sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                }
                accelerometer?.let {
                        sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                }
        }

        fun stopSensors() {
                sensorManager.unregisterListener(this)
        }

        override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                                val x = event.values[0]
                                val y = event.values[1]
                                val z = event.values[2]
                                println("Accelerometer: ${x}, ${y}, $z")
                        // Process accelerometer data
                        processAccelerometerData(x, y, z)
                        }
                        Sensor.TYPE_GYROSCOPE -> {
                                val rotationX = event.values[0]
                                val rotationY = event.values[1]
                                val rotationZ = event.values[2]
                                val timestamp = event.timestamp
                                println("Gyroscope: ${rotationX}, ${rotationY}, ${rotationZ}, $timestamp")

                        // Process gyroscope data
                        processGyroscopeData(rotationX, rotationY, rotationZ, timestamp)
                        }
                }

        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                //
        }

        private val accelerationData = mutableListOf<Float>()
        private val samplingRate = 0.02f // Example: 50 Hz (1/50 seconds)

        private fun processAccelerometerData(x: Float, y: Float, z: Float) {
                // Filter and collect Z-axis data
                val filteredZ = z - 9.8f
                accelerationData.add(filteredZ)

                if (accelerationData.size > 100) { // Process data after collecting enough samples
                        val displacement = calculateDisplacement(accelerationData, samplingRate)
                        val frequency = calculateFrequency(accelerationData, 1 / samplingRate)
                        println("Wave Height: ${displacement.maxOrNull()}")
                        println("Wave Frequency: $frequency")

                        displacement.maxOrNull()?.let { updateHeight(it) }
                        updateFrequency(frequency)
                        // Clear data for the next batch
                        accelerationData.clear()
                }
        }

        // Previous timestamp to calculate time intervals
        private var lastTimestamp: Long? = null

        private fun processGyroscopeData(
                rotationX: Float,
                rotationY: Float,
                rotationZ: Float,
                timestamp: Long
        ) {
                // Calculate the time difference (dt) in seconds
                val dt = lastTimestamp?.let { (timestamp - it) / 1_000_000_000.0f } ?: 0f
                lastTimestamp = timestamp

                if (dt > 0) {
                        // Pass raw angular velocities and time interval to the tilt calculation function
                        val tilt = calculateTilt(rotationX, rotationY, rotationZ, dt)

                        // Log or use the calculated tilt values
                        println("Tilt X: ${tilt.first}°")
                        println("Tilt Y: ${tilt.second}°")
                        println("Tilt Z: ${tilt.third}°")

                        updateTilt(tilt)
                }
        }
}