package com.example.wavereader.ui.main

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wavereader.model.GraphDisplayOptions
import com.example.wavereader.ui.graph.SensorGraph
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
            // Display Data
                ShowRecordData(uiState = uiState)
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
    var displayOptions by remember { mutableStateOf(GraphDisplayOptions()) }

    val height = uiState.height
    val period = uiState.period
    val direction = uiState.direction

    Column(modifier = Modifier.padding(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Height", fontWeight = FontWeight.Bold)
                        Text("${height ?: "-"} ft")
                    }
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Period", fontWeight = FontWeight.Bold)
                        Text("${period ?: "-"} s")
                    }
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Direction", fontWeight = FontWeight.Bold)
                        Text("${direction ?: "-"}°")
                    }
                }
            }
            // Filter Graph display
            DropDownFilterGraphView(displayOptions, onUpdate = { displayOptions = it })

            Box(
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            ) {
                SensorGraph(uiState.measuredWaveList, displayOptions)
            }
        }
    }
}


@Composable
fun DropDownFilterGraphView(
    displayOptions: GraphDisplayOptions,
    onUpdate: (GraphDisplayOptions) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = !expanded }) {
            Text("Graph Filters")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand graph filter options"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(min = 220.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Toggle Graph Lines",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wave Height", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = displayOptions.showHeight,
                        onCheckedChange = { onUpdate(displayOptions.copy(showHeight = it)) }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wave Period", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = displayOptions.showPeriod,
                        onCheckedChange = { onUpdate(displayOptions.copy(showPeriod = it)) }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wave Direction", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = displayOptions.showDirection,
                        onCheckedChange = { onUpdate(displayOptions.copy(showDirection = it)) }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wave Forecast", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = displayOptions.showForecast,
                        onCheckedChange = { onUpdate(displayOptions.copy(showForecast = it)) }
                    )
                }
            }
        }
    }
}
