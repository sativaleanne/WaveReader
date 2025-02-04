package com.example.wavereader.viewmodels

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wavereader.model.MeasuredWaveData
import com.example.wavereader.waveCalculator.calculateWaveHeight
import com.example.wavereader.waveCalculator.calculateWavePeriod
import com.example.wavereader.testData.FakeMeasuredWaveData
import com.example.wavereader.waveCalculator.calculateWaveDirection
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WaveUiState(
        val measuredWaveList: List<MeasuredWaveData> = emptyList(),
        val height: Float? = null,
        val period: Float? = null,
        val direction: Float? = null,
)

class SensorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

        //For testing purposes
        private val generateFakeData = FakeMeasuredWaveData()
        private var job: Job? = null

        private val _uiState = MutableStateFlow(WaveUiState())
        val uiState: StateFlow<WaveUiState> = _uiState.asStateFlow()

        // Get reference to the sensor service
        private val sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Previous timestamp to calculate time intervals
        private var lastTimestamp: Long = 0L

        private val accelerationData = mutableListOf<Array<Float>>()
        private val verticalAcceleration = mutableListOf<Float>()
        private var samplingRate: Float = 0f
        private var dt: Float = 0f

        //For testing purposes
        fun startFakeWaveData(){
//                for (i in fakeSensorData.indices) {
//                        val x = fakeSensorData[i].ax
//                        val y = fakeSensorData[i].ay
//                        val z = fakeSensorData[i].az
//                        filterData(x, y, z)
//                }
                job = viewModelScope.launch {
                        generateFakeData.generateWaveData()
                                .collect {      fakeWaveData ->
                                        updateMeasuredWaveData(fakeWaveData.waveHeight, fakeWaveData.wavePeriod, fakeWaveData.waveDirection)

                                }
                }
        }

        fun stopFakeWaveData(){
                job?.cancel()
        }

        private fun updateMeasuredWaveData(height: Float, period: Float, direction: Float) {

                //Add latest data
                val newMeasuredWaveData = MeasuredWaveData(height, period, direction)
                updateHeight(height)
                updatePeriod(period)
                updateDirection(direction)

                //Update state and remove oldest
                _uiState.update { currentState ->
                        currentState.copy(
                                measuredWaveList = (currentState.measuredWaveList + newMeasuredWaveData).takeLast(50)
                        )
                }
        }


        private fun updateHeight(height: Float) {
                _uiState.update { currentState ->
                        currentState.copy(
                                height = height
                        )
                }
        }

        private fun updatePeriod(period: Float) {
                _uiState.update { currentState ->
                        currentState.copy(
                                period = period
                        )
                }
        }

        private fun updateDirection(direction: Float) {
                _uiState.update { currentState ->
                        currentState.copy(
                                direction = direction
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
                //Adjust sampling rate and timestamp
                val currentTimestamp = event.timestamp
                if (lastTimestamp != 0L) {
                        //TODO seperate out
                        dt = (currentTimestamp - lastTimestamp) / 1000000000f
                        samplingRate = 1.0f / ((currentTimestamp - lastTimestamp) / 1000000000f)
                        println("Sampling Rate: $samplingRate")
                        println("TimeStep dt: $dt")
                }
                lastTimestamp = currentTimestamp


                when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                                val x = event.values[0]
                                val y = event.values[1]
                                val z = event.values[2]
                                println("Accelerometer: ${x}, ${y}, $z")
                                filterData(x, y, z)
                        }
                        Sensor.TYPE_GYROSCOPE -> {
                                val rotationX = event.values[0]
                                val rotationY = event.values[1]
                                val rotationZ = event.values[2]
                                val timestamp = event.timestamp
                                println("Gyroscope: ${rotationX}, ${rotationY}, ${rotationZ}, $timestamp")
                                //TODO gyroscope filtering and processing
                        }


                }

        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                //
        }

        private fun filterData(x: Float, y: Float, z: Float) {

                //Filter out the gravity difference
                val gravity = 9.8f
                val linearAcceleration = Array<Float>(3){ 0f }

                linearAcceleration[0] = x - gravity;
                linearAcceleration[1] = y - gravity;
                linearAcceleration[2] = z - gravity;

                // collect XYZ-axis data and Z-axis
                accelerationData.add(linearAcceleration)
                verticalAcceleration.add(linearAcceleration[2])

                // Process data after collecting enough samples (10 seconds from 60,000 microseconds)
                if ((accelerationData.size > 170) and (verticalAcceleration.size > 170)) {
                        accelerationData.removeAt(0)
                        verticalAcceleration.removeAt(0)
                        processData()
                }
        }


        private fun processData() {
                val waveHeight = calculateWaveHeight(verticalAcceleration, dt)
                val wavePeriod = calculateWavePeriod(verticalAcceleration, samplingRate)
                val waveDirection = calculateWaveDirection(accelerationData)
                println("Wave Height: $waveHeight")
                println("Wave Period: $wavePeriod")
                println("Wave Direction: $waveDirection")

                updateHeight(waveHeight)
                updatePeriod(wavePeriod)
                updateDirection(waveDirection)
                updateMeasuredWaveData(waveHeight, wavePeriod, waveDirection)
        }

}