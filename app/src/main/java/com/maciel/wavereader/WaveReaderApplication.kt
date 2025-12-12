package com.maciel.wavereader

import android.app.Application
import com.maciel.wavereader.data.AppContainer
import com.maciel.wavereader.data.DefaultAppContainer

class WaveReaderApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }
}