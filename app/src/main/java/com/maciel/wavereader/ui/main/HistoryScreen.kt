package com.maciel.wavereader.ui.main

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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.maciel.wavereader.model.HistoryRecord
import com.maciel.wavereader.model.MeasuredWaveData
import com.maciel.wavereader.ui.components.HistoryFilterPanel
import com.maciel.wavereader.ui.graph.HistoryGraph
import com.maciel.wavereader.utils.exportToCsv
import com.maciel.wavereader.utils.exportToJson
import com.maciel.wavereader.viewmodels.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            LazyColumn(modifier = Modifier.fillMaxHeight(0.9f)) {
                items(historyData) { record ->
                    HistoryCard(record, expandedItems.contains(record.id)) {
                        viewModel.toggleItemExpansion(record.id)
                    }
                }
            }
//            Spacer(modifier = Modifier.height(16.dp))
//            SummaryDisplay(historyData)
        }
    }
}

@Composable
fun DropDownFilterButton(viewModel: HistoryViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = !expanded }, elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)) {
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
        Button(onClick = { expanded = !expanded }, elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)) {
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
            //TODO: just display average
            if (isExpanded) {
                val avgHeight = record.dataPoints.map { it.height }.average()
                val avgPeriod = record.dataPoints.map { it.period }.average() * 100
                val avgDirection = record.dataPoints.map { it.direction }.average()

                Text("Ave. Height: %.1f ft".format(avgHeight))
                Text("Ave. Period: %.1f s".format(avgPeriod))
                Text("Ave. Direction: %.1f°".format(avgDirection))

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
// Not useful in it's current state
@Composable
fun SummaryDisplay( historyData: List<HistoryRecord> ) {
    val summaryData = historyData.mapIndexedNotNull { index, record ->
        val points = record.dataPoints
        if (points.isEmpty()) return@mapIndexedNotNull null

        val avgHeight = points.map { it.height }.average().toFloat()
        val avgPeriod = points.map { it.period }.average().toFloat()
        val avgDirection = points.map { it.direction }.average().toFloat()

        MeasuredWaveData(
            waveHeight = avgHeight,
            wavePeriod = avgPeriod,
            waveDirection = avgDirection,
            time = index.toFloat()
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Summary of All Sessions", style = MaterialTheme.typography.titleMedium)
        HistoryGraph(waveData = summaryData, isInteractive = true)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun timestamp(): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    return sdf.format(Date())
}



