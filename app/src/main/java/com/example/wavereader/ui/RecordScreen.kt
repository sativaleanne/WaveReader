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
import androidx.compose.ui.unit.sp
import com.example.wavereader.viewmodels.WaveUiState
import com.example.wavereader.viewmodels.WaveViewModel

@Composable
fun RecordDataScreen(
    viewModel: WaveViewModel,
    uiState: WaveUiState
) {
    var isSensorActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShowRecordData(viewModel = viewModel, uiState = uiState)
        Spacer(modifier = Modifier)
        // Toggle Button
        Button(
            modifier = Modifier.padding(16.dp),
            //.align(Alignment.CenterHorizontally),
            onClick = {
                isSensorActive = !isSensorActive
                if (isSensorActive) {
                    viewModel.startSensors()
                } else {
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
    viewModel: WaveViewModel,
    uiState: WaveUiState
) {
    Column(
        modifier = Modifier
            .padding(16.dp),
    ) {
        // Tilt X
        Text(
            text = "Tilt X: %.2f°".format(uiState.tiltX),
            fontSize = 18.sp,
            modifier = Modifier//.padding(bottom = 4.dp)
        )
        // Tilt Y
        Text(
            text = "Tilt Y: %.2f°".format(uiState.tiltY),
            fontSize = 18.sp,
            modifier = Modifier
        )
        // Tilt Z
        Text(
            text = "Tilt Z: %.2f°".format(uiState.tiltZ),
            fontSize = 18.sp,
            modifier = Modifier
        )
        Text(
            text = "Height: %.2f°".format(uiState.height),
            fontSize = 18.sp,
            modifier = Modifier
        )
        Text(
            text = "Frequency: %.2f°".format(uiState.frequency),
            fontSize = 18.sp,
            modifier = Modifier
        )
    }
}