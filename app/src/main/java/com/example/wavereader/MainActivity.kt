package com.example.wavereader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.wavereader.ui.theme.WaveReaderTheme
import com.example.wavereader.viewmodels.SensorViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: SensorViewModel by viewModels()
        enableEdgeToEdge()
        setContent {
            WaveReaderTheme {
                WaveApp(
                    viewModel = viewModel,
                )
            }
        }
    }
}


