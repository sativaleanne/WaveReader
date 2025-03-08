package com.example.wavereader.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
        if(viewModel.checkSensors()) {
            ShowRecordData(uiState = uiState)
            Spacer(modifier = Modifier)
            // Toggle Button
            SensorButton(viewModel)
            if(uiState.measuredWaveList.isNotEmpty()) {
                ClearButton(viewModel)
            }
        }
        else {
            ShowSensorErrorScreen()
        }

    }
}

@Composable
fun ShowSensorErrorScreen() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Unable to use this feature due to missing sensors!"
        )
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
                viewModel.startSensors()
            } else {
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
fun ClearButton(
    viewModel: SensorViewModel
) {
    Button(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(9.dp),
        onClick = {
            if (viewModel.uiState.value.measuredWaveList.isNotEmpty()) {
                viewModel.clearMeasuredWaveData()
            }
        },
        elevation = ButtonDefaults.buttonElevation(1.dp)
    ) {
        Text(text = "Clear")
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
            Column(modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Wave Height: ${it.height?: 0f} feet")
                Text("Wave Period: ${it.period?: 0f} seconds")
                Text("Wave Direction: ${it.direction?: 0f} degrees")
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) {
                    DrawSensorGraph(it.measuredWaveList)
                }
            }
        }
    }
}