package com.example.wavereader.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wavereader.viewmodels.WaveUiState
import com.example.wavereader.viewmodels.SensorViewModel

@Composable
fun RecordDataScreen(
    viewModel: SensorViewModel,
    uiState: WaveUiState
) {
    var isSensorActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShowRecordData(uiState = uiState)
        Spacer(modifier = Modifier)
        // Toggle Button
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = {
                isSensorActive = !isSensorActive
                if (isSensorActive) {
                   // viewModel.startFakeWaveData()
                    viewModel.startSensors()
                } else {
                    //viewModel.stopFakeWaveData()
                    viewModel.stopSensors()
                }
            }
        ) {
            Text(text = if (isSensorActive) "Pause Sensors" else "Resume Sensors")
        }
    }
}

@Composable
fun ShowRecordData(
    uiState: WaveUiState
) {
    Column(
        modifier = Modifier
            .padding(16.dp),
    ) {
        uiState.let {
            Column {
                Text("Wave Height: ${it.height} feet")
                Text("Wave Period: ${it.period} seconds")
                Text("Wave Direction: ${it.direction} degrees")
                DrawSensorGraph(it.measuredWaveList)
            }
        }
    }
}