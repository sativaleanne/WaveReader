package com.example.wavereader

import android.app.Application
import com.example.wavereader.data.AppContainer
import com.example.wavereader.data.DefaultAppContainer

class WaveReaderApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}