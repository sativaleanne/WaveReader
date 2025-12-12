package com.maciel.wavereader.ui.main

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.maciel.wavereader.model.GraphDisplayOptions
import com.maciel.wavereader.ui.components.AlertConfirm
import com.maciel.wavereader.ui.components.DropDownFilterGraphView
import com.maciel.wavereader.ui.components.WaveDataCard
import com.maciel.wavereader.ui.graph.SensorGraph
import com.maciel.wavereader.utils.RequestLocationPermission
import com.maciel.wavereader.viewmodels.LocationViewModel
import com.maciel.wavereader.viewmodels.SensorViewModel
import com.maciel.wavereader.viewmodels.WaveUiState

/*
* Record Screen for utilizing sensors and measuring waves in real time.
 */
@Composable
fun RecordDataScreen(
    viewModel: SensorViewModel,
    uiState: WaveUiState,
    isGuest: Boolean
) {
    val locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
    var isSensorActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewModel.checkSensors()) {
            // Display Data
            ShowRecordData(uiState = uiState, viewModel = viewModel)
            if (isSensorActive and uiState.measuredWaveList.isEmpty()) {
                Text("Collecting Data...")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // BUTTON ROW
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SensorButton(isSensorActive) { active ->
                    isSensorActive = active
                    if (active) viewModel.startSensors()
                    else viewModel.stopSensors()
                }
                if (uiState.measuredWaveList.isNotEmpty()) {
                    ClearButton(viewModel)
                }
                if (uiState.measuredWaveList.isNotEmpty() && !isGuest && !isSensorActive) {
                    SaveButton(viewModel, locationViewModel)
                }
            }
        } else {
            ShowSensorErrorScreen()
        }
    }
}

// Error if sensors unavailable
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

// Start/Start Sensors
@Composable
fun SensorButton(
    isSensorActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Button(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(9.dp),
        onClick = { onToggle(!isSensorActive) },
        elevation = ButtonDefaults.buttonElevation(1.dp)
    ) {
        Text(text = if (isSensorActive) "Pause" else "Record")
    }
}

// Save data to firebase with location
@Composable
fun SaveButton(
    viewModel: SensorViewModel,
    locationViewModel: LocationViewModel
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isSaving by remember { mutableStateOf(false) }

    RequestLocationPermission(
        onDenied = {
            Toast.makeText(context, "Location permission denied.", Toast.LENGTH_SHORT).show()
        }
    ) {
        Button(
            modifier = Modifier.padding(8.dp),
            shape = RoundedCornerShape(9.dp),
            onClick = {
                locationViewModel.fetchLocationAndSave(
                    context,
                    fusedLocationClient,
                    viewModel,
                    onSavingStarted = { isSaving = true },
                    onSavingFinished = { isSaving = false },
                    onSaveSuccess = {
                        Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            enabled = !isSaving,
            elevation = ButtonDefaults.buttonElevation(1.dp)
        ) {
            Text(if (isSaving) "Saving..." else "Save")
        }
    }
}

// Clear data from graph and database
@Composable
fun ClearButton(viewModel: SensorViewModel) {
    var showDialog by remember { mutableStateOf(false) }

    Column {
        Button(
            modifier = Modifier.padding(8.dp),
            shape = RoundedCornerShape(9.dp),
            onClick = { showDialog = true },
            elevation = ButtonDefaults.buttonElevation(1.dp)
        ) {
            Text("Clear")
        }

        if (showDialog) {
            AlertConfirm(
                onDismissRequest = { showDialog = false },
                onConfirmation = {
                    viewModel.clearMeasuredWaveData()
                    showDialog = false
                },
                dialogTitle = "Clear Data?",
                dialogText = "Are you sure you want to delete all recorded wave data?",
                icon = Icons.Default.Warning
            )
        }
    }
}

// Display data in graph and text
@Composable
fun ShowRecordData(
    uiState: WaveUiState,
    viewModel: SensorViewModel
) {
    var displayOptions by remember { mutableStateOf(GraphDisplayOptions()) }

    val height = uiState.height
    val period = uiState.period
    val direction = uiState.direction

    Column(modifier = Modifier.padding(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Display Wave Data
            WaveDataCard(
                "Average Conditions",
                listOf(height, period, direction),
                listOf("Height", "Period", "Direction"),
                listOf("ft", "s", "°")
            )
            // Filter Graph display
            DropDownFilterGraphView(displayOptions, onUpdate = { displayOptions = it })

            Column(modifier = Modifier.fillMaxWidth()) {
                SensorGraph(uiState.measuredWaveList, displayOptions)
            }
        }
    }
}

