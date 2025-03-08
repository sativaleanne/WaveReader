package com.example.wavereader.ui

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.wavereader.data.HistoryRepository
import com.example.wavereader.model.HistoryRecord
import com.example.wavereader.model.MeasuredWaveData
import com.example.wavereader.utils.exportToCsv
import com.example.wavereader.utils.exportToJson
import com.example.wavereader.viewmodels.HistoryViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*
* History Screen for users with accounts to view past recorded data.
* TODO: SEPARATE THINGS, logic and ui
 */
@Composable
fun HistoryScreen(navController: NavHostController) {

    val viewModel: HistoryViewModel = viewModel()
    val historyData by viewModel.historyData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val expandedItems by viewModel.expandedItems.collectAsState()

    val historyRepository = remember { HistoryRepository() }

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    BackHandler {
        navController.popBackStack()
    }

    var selectedDate by remember { mutableStateOf(getTodayDate()) }

    val context = LocalContext.current

    // Get current users data from firestore
    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton( onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Text("History", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // TODO: FILTER DATA BY DATE OR SOMETHING ELSE
        DatePickerField(selectedDate) { selectedDate = it }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // TODO: ADD FILTERS
            DropDownFilterButton()

            // TODO: FINISH CVS JSON
            DropDownExportButton(context, historyData)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (historyData.isEmpty()) {
            Text("No history data found.")
        } else {
            LazyColumn(modifier = Modifier.fillMaxHeight(0.4f)) {
                items(historyData) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(record.timestamp)
                                    Text(record.location)
                                    // TODO: FIX HOW DATA IS DISPLAYED
//                                    record.lat?.let { lat ->
//                                        record.lon?.let { lon ->
//                                            Text("Lat: %.4f, Lon: %.4f".format(lat, lon))
//                                        }
//                                    }
                                }
                                IconButton(onClick = {
                                    viewModel.toggleItemExpansion(record.id)
                                }) {
                                    Icon(
                                        if (expandedItems.contains(record.id)) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand"
                                    )
                                }
                            }

                            if (expandedItems.contains(record.id)) {
                                Text("Height: ${record.dataPoints.maxOfOrNull { it.height } ?: 0} ft")
                                Text("Period: ${record.dataPoints.maxOfOrNull { it.period } ?: 0} s")
                                Text("Direction: ${record.dataPoints.maxOfOrNull { it.direction } ?: 0}°")

                                Spacer(modifier = Modifier.height(8.dp))

                                val sessionDataPoints = record.dataPoints.map { dataPoint ->
                                    MeasuredWaveData(
                                        waveHeight = dataPoint.height,
                                        wavePeriod = dataPoint.period,
                                        waveDirection = dataPoint.direction,
                                        time = dataPoint.time
                                    )
                                }
                                // TODO: Remove Graph interaction
                                DrawHistoryGraph(sessionDataPoints)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // TODO: MAKE SMALLER OR SOMETHING
            //DrawServiceGraph(waveData = fakeWaveData) // TODO: Replace fakeWaveData with actual combined data!
        }
    }
}

@Composable
fun DropDownExportButton(context: Context, historyData: List<HistoryRecord>) {
    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            exportToCsv(context, uri, historyData)
        }
    }
    val createJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            exportToJson(context, uri, historyData)
        }
    }
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Button(onClick = { expanded = !expanded }) {
            Text("Export")
            Icon(
                if(!expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = "Expand Icon",
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Export to JSON") },
                onClick = {
                    if (historyData.isNotEmpty()) createJsonLauncher.launch("wave_data_${timestamp()}.json")
                }
            )
            DropdownMenuItem(
                text = { Text("Export to CVS") },
                onClick = {
                    if (historyData.isNotEmpty()) createCsvLauncher.launch("wave_data_${timestamp()}.csv")
                }
            )
        }
    }
}

@Composable
fun DropDownFilterButton() {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Button(onClick = { expanded = !expanded }) {
            Text("Filter")
            Icon(
                if(!expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = "Filter Icon",
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Date") },
                onClick = {
                    //TODO
                }
            )
            DropdownMenuItem(
                text = { Text("Location") },
                onClick = {
                    //TODO
                }
            )
        }
    }
}

@Composable
fun DatePickerField(selectedDate: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear)
            onDateSelected(formattedDate)
        },
        year, month, day
    )

    OutlinedTextField(
        value = selectedDate,
        onValueChange = {},
        label = { Text("Date") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Pick Date")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

fun getTodayDate(): String {
    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return sdf.format(Date())
}

fun timestamp(): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    return sdf.format(Date())
}

@Preview
@Composable
fun DropDownButtonPreview() {
    DropDownFilterButton()
}



