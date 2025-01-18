package com.example.wavereader

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

data class WaveUiState(
        val tiltX: Float? = null,
        val tiltY: Float? = null,
        val tiltZ: Float? = null

)

class WaveViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
        // Get reference to the sensor service
        private val sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        private val _tiltX = mutableStateOf(0f)
        val tiltX: State<Float> = _tiltX

        private val _tiltY = mutableStateOf(0f)
        val tiltY: State<Float> = _tiltY

        private val _tiltZ = mutableStateOf(0f)
        val tiltZ: State<Float> = _tiltZ


        fun updateTilt(tilt: Triple<Float, Float, Float>) {
                _tiltX.value = tilt.first
                _tiltY.value = tilt.second
                _tiltZ.value = tilt.third
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
                        //processAccelerometerData(x, y, z)
                        }
                        Sensor.TYPE_GYROSCOPE -> {
                                val rotationX = event.values[0]
                                val rotationY = event.values[1]
                                val rotationZ = event.values[2]
                                val timestamp = event.timestamp
                                println("Gyroscope: ${rotationX}, ${rotationY}, ${rotationZ}, $timestamp")

                        // Process gyroscope data
                        //processGyroscopeData(rotationX, rotationY, rotationZ, timestamp)
                        }
                }

        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                //
        }
}