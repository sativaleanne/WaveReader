package com.example.wavereader.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wavereader.model.SortOrder
import com.example.wavereader.model.HistoryFilterState
import java.text.SimpleDateFormat
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.rememberDateRangePickerState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFilterPanel(
    initialFilter: HistoryFilterState = HistoryFilterState(),
    onApply: (HistoryFilterState) -> Unit
) {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    var locationText by remember { mutableStateOf(initialFilter.locationQuery) }
    var startDateMillis by remember { mutableStateOf(initialFilter.startDateMillis) }
    var endDateMillis by remember { mutableStateOf(initialFilter.endDateMillis) }
    var startDateText by remember { mutableStateOf(initialFilter.startDateMillis?.let { dateFormat.format(Date(it)) } ?: "") }
    var endDateText by remember { mutableStateOf(initialFilter.endDateMillis?.let { dateFormat.format(Date(it)) } ?: "") }

    var sortOrder by remember { mutableStateOf(initialFilter.sortOrder) }
    var expandedSort by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Location Field
        OutlinedTextField(
            value = locationText,
            onValueChange = { locationText = it },
            label = { Text("Filter by Location") },
            modifier = Modifier.fillMaxWidth()
        )

        // Date Range Picker
        TextButton(onClick = { showDateRangePicker = true }) {
            Text(
                text = if (startDateText.isNotBlank() && endDateText.isNotBlank())
                    "From $startDateText to $endDateText"
                else "Select Date Range"
            )
        }

        if (showDateRangePicker) {
            DatePickerDialog(
                onDismissRequest = { showDateRangePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            startDateMillis = dateRangePickerState.selectedStartDateMillis
                            endDateMillis = dateRangePickerState.selectedEndDateMillis
                            startDateText = startDateMillis?.let { dateFormat.format(Date(it)) } ?: ""
                            endDateText = endDateMillis?.let { dateFormat.format(Date(it)) } ?: ""
                            showDateRangePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDateRangePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    showModeToggle = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .padding(16.dp)
                )
            }
        }

        // Sort Order Dropdown
        OutlinedButton(
            onClick = { expandedSort = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sort: ${if (sortOrder == SortOrder.DATE_DESCENDING) "Newest First" else "Oldest First"}")
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expandedSort,
            onDismissRequest = { expandedSort = false }
        ) {
            DropdownMenuItem(
                text = { Text("Newest First") },
                onClick = {
                    sortOrder = SortOrder.DATE_DESCENDING
                    expandedSort = false
                }
            )
            DropdownMenuItem(
                text = { Text("Oldest First") },
                onClick = {
                    sortOrder = SortOrder.DATE_ASCENDING
                    expandedSort = false
                }
            )
        }

        // Apply Button
        Button(onClick = {
            onApply(
                HistoryFilterState(
                    locationQuery = locationText.trim(),
                    startDateMillis = startDateMillis,
                    endDateMillis = endDateMillis,
                    sortOrder = sortOrder
                )
            )
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Apply Filter")
        }
    }
}


