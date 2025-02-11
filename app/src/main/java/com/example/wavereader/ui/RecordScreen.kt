package com.example.wavereader.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.wavereader.R
import com.example.wavereader.viewmodels.WaveUiState
import com.example.wavereader.viewmodels.SensorViewModel

@Composable
fun RecordDataScreen(
    viewModel: SensorViewModel,
    uiState: WaveUiState
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShowRecordData(uiState = uiState)
        Spacer(modifier = Modifier)
        // Toggle Button
        SensorButton(viewModel)
    }
}

@Composable
fun SensorButton(
    viewModel: SensorViewModel
) {
    var isSensorActive by remember { mutableStateOf(false) }
    Button(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(9.dp),
        onClick = {
            isSensorActive = !isSensorActive
            if (isSensorActive) {
                //viewModel.startFakeWaveData()
                viewModel.startSensors()
            } else {
                //viewModel.stopFakeWaveData()
                viewModel.stopSensors()
            }
        },
        elevation = ButtonDefaults.buttonElevation(1.dp)
    ) {
        Text(text = if (isSensorActive) stringResource(R.string.pause_sensors_button) else stringResource(
            R.string.record_button
        )
        )
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