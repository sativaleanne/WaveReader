package com.example.wavereader.sensors

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle

class SensorActivity : Activity(), SensorEventListener {
    // Get reference to the sensor service
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
//    val triggerEventListener = object : TriggerEventListener() {
//        override fun onTrigger(event: TriggerEvent?) {
//
//        }
//    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView()
        createManager()
    }

    // check for both sensors
    fun hasSensors(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null &&
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
    }


    //create instance of sensor manager class
    private fun createManager() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                // Process accelerometer data (e.g., filter and analyze)
            }
            Sensor.TYPE_GYROSCOPE -> {
                val rotationX = event.values[0]
                val rotationY = event.values[1]
                val rotationZ = event.values[2]
                // Process gyroscope data
            }
        }

    }

    override fun onResume() {
        super.onResume()
        //TODO("Not yet implemented")
        accelerometer?.also { accelerometer ->
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        //TODO("Not yet implemented")
        gyroscope?.also { gyroscope ->
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

}