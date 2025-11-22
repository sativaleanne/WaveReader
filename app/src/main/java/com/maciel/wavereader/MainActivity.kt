package com.maciel.wavereader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.maciel.wavereader.ui.theme.WaveReaderTheme
import com.maciel.wavereader.viewmodels.SensorViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the repository from the container
        val container = (application as WaveReaderApplication).container
        val firestoreRepository = container.firestoreRepository

        // Create ViewModel with factory
        val viewModel: SensorViewModel by viewModels {
            SensorViewModel.provideFactory(application, firestoreRepository)
        }

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


