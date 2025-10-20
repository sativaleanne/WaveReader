package com.maciel.wavereader.ui.main

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.tooling.preview.Preview
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavHostController) {
    val viewModel: HistoryViewModel = viewModel()
    val historyData by viewModel.historyData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val expandedItems by viewModel.expandedItems.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedItems by viewModel.selectedItems.collectAsState()

    val context = LocalContext.current

    BackHandler {
        if (isSelectionMode) {
            viewModel.clearSelection()
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setDefaultRecentFilter()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSelectionMode) {
                SelectionTopBar(
                    selectedCount = selectedItems.size,
                    onCancel = { viewModel.clearSelection() },
                    onDelete = { /* handle delete */ },
                    onExport = { /* handle export */ }
                )
            } else {
                // Buttons for filter, sort, or export
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DropDownFilterButton(viewModel)
                    DropDownExportButton(context, historyData)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {

                CircularProgressIndicator()

            } else if (historyData.isEmpty()) {

                Text("No history data found.")

            } else {
                LazyColumn(modifier = Modifier.fillMaxHeight(0.9f)) {
                    items(historyData) { record ->
                        NewHistoryCard(
                            record = record,
                            isExpanded = expandedItems.contains(record.id),
                            onToggle = { viewModel.toggleItemExpansion(record.id) },
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedItems.contains(record.id),
                            onLongClick = { viewModel.enableSelectionMode(record.id) },
                            onClick = {
                                if (isSelectionMode) {
                                    viewModel.toggleItemSelection(record.id)
                                } else {
                                    viewModel.toggleItemExpansion(record.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel Selection")
            }
            Text("$selectedCount selected", style = MaterialTheme.typography.titleMedium)
        }
        Row {
            IconButton(onClick = onExport) {
                Icon(Icons.Default.Download, contentDescription = "Export")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewHistoryCard(
    record: HistoryRecord,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick
            ),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.timestamp)
                    Text(record.location)
                }
                if (!isSelectionMode) {
                    IconButton(onClick = onToggle) {
                        Icon(
                            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand"
                        )
                    }
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




