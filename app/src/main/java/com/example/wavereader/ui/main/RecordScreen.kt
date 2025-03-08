package com.example.wavereader.ui.main

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wavereader.ui.DrawSensorGraph
import com.example.wavereader.utils.RequestLocationPermission
import com.example.wavereader.viewmodels.LocationViewModel
import com.example.wavereader.viewmodels.SensorViewModel
import com.example.wavereader.viewmodels.WaveUiState
import com.google.android.gms.location.LocationServices

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
            ShowRecordData(uiState = uiState)

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

//Error if sensors unavailable
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
// TODO: SEPARATE PERMISSIONS
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
fun ClearButton(
    viewModel: SensorViewModel
) {
    Button(
        modifier = Modifier.padding(8.dp),
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

// Display data in graph and text
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