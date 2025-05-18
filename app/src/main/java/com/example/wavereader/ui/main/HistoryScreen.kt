package com.example.wavereader.ui.main

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.wavereader.model.*
import com.example.wavereader.ui.components.HistoryFilterPanel
import com.example.wavereader.ui.graph.HistoryGraph
import com.example.wavereader.utils.exportToCsv
import com.example.wavereader.utils.exportToJson
import com.example.wavereader.viewmodels.HistoryViewModel
import com.example.wavereader.viewmodels.LocationViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(navController: NavHostController) {
    val viewModel: HistoryViewModel = viewModel()
    val historyData by viewModel.historyData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val expandedItems by viewModel.expandedItems.collectAsState()

    val context = LocalContext.current

    BackHandler { navController.popBackStack() }

    LaunchedEffect(Unit) {
        viewModel.setDefaultRecentFilter()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Text("History", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons for filter, sort, or export
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DropDownFilterButton(viewModel)
            DropDownExportButton(context, historyData)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {

            CircularProgressIndicator()

        } else if (historyData.isEmpty()) {

            Text("No history data found.")

        } else {
            LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                items(historyData) { record ->
                    HistoryCard(record, expandedItems.contains(record.id)) {
                        viewModel.toggleItemExpansion(record.id)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            SummaryDisplay(historyData)
        }
    }
}

@Composable
fun DropDownFilterButton(viewModel: HistoryViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = !expanded }) {
            Text("Filter")
            Icon(
                if (!expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = "Expand Icon"
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column(Modifier.padding(12.dp)) {
                HistoryFilterPanel(
                    initialFilter = viewModel.filterState.collectAsState().value,
                    onApply = {
                        viewModel.updateFilter(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DropDownExportButton(context: Context, historyData: List<HistoryRecord>) {
    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? -> uri?.let { exportToCsv(context, it, historyData) } }

    val createJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> uri?.let { exportToJson(context, it, historyData) } }

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(8.dp)) {
        Button(onClick = { expanded = !expanded }) {
            Text("Export")
            Icon(
                if (!expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = "Expand Icon"
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Export to JSON") }, onClick = {
                if (historyData.isNotEmpty()) createJsonLauncher.launch("wave_data_${timestamp()}.json")
            })
            DropdownMenuItem(text = { Text("Export to CSV") }, onClick = {
                if (historyData.isNotEmpty()) createCsvLauncher.launch("wave_data_${timestamp()}.csv")
            })
        }
    }
}

// Each data record gets a card view.
@Composable
fun HistoryCard(
    record: HistoryRecord,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(record.timestamp)
                    Text(record.location)
                }
                IconButton(onClick = onToggle) {
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand"
                    )
                }
            }

            if (isExpanded) {
                Text("Max Height: ${record.dataPoints.maxOfOrNull { it.height } ?: 0} ft")
                Text("Max Period: ${record.dataPoints.maxOfOrNull { it.period } ?: 0} s")
                Text("Max Direction: ${record.dataPoints.maxOfOrNull { it.direction } ?: 0}°")

                Spacer(modifier = Modifier.height(8.dp))

                val sessionDataPoints = record.dataPoints.map {
                    MeasuredWaveData(
                        waveHeight = it.height,
                        wavePeriod = it.period,
                        waveDirection = it.direction,
                        time = it.time
                    )
                }
                HistoryGraph(waveData = sessionDataPoints, isXLabeled = false)
            }
        }
    }
}

// Summary Graph and view
// TODO: X AXIS
@Composable
fun SummaryDisplay( historyData: List<HistoryRecord> ) {
    val summaryData = historyData.mapNotNull { record ->
        val points = record.dataPoints
        if (points.isEmpty()) return@mapNotNull null

        val avgHeight = points.map { it.height }.average().toFloat()
        val avgPeriod = points.map { it.period }.average().toFloat()
        val avgDirection = points.map { it.direction }.average().toFloat()

        MeasuredWaveData(
            waveHeight = avgHeight,
            wavePeriod = avgPeriod,
            waveDirection = avgDirection,
            time = 0f
        )
    }
    Text("Summary of All Sessions", style = MaterialTheme.typography.titleMedium)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(top = 8.dp)
    ) {
        HistoryGraph(waveData = summaryData)
    }
}

fun timestamp(): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    return sdf.format(Date())
}



