package com.example.wavereader.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wavereader.model.ApiVariable
import com.example.wavereader.model.GraphDisplayOptions
import com.example.wavereader.model.HistoryFilterState
import com.example.wavereader.model.SortOrder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

@Composable
fun DropDownFilterSearchButton(
    allVariables: List<ApiVariable>,
    selectedVariables: Set<ApiVariable>,
    onUpdate: (Set<ApiVariable>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = !expanded }
        ) {
            Text("Filter Options")
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand filter options"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(min = 220.dp)
        ) {
            Column(
                Modifier
                    .padding(12.dp)
            ) {
                Text(
                    text = "Select Data Types",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                allVariables.forEach { variable ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = variable in selectedVariables,
                            onCheckedChange = {
                                val updated = if (it)
                                    selectedVariables + variable
                                else
                                    selectedVariables - variable
                                onUpdate(updated)
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = variable.label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// TODO: Update location using locationviewmodel geocoding.
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
    var startDateText by remember { mutableStateOf(initialFilter.startDateMillis?.let { dateFormat.format(
        Date(it)
    ) } ?: "") }
    var endDateText by remember { mutableStateOf(initialFilter.endDateMillis?.let { dateFormat.format(
        Date(it)
    ) } ?: "") }

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

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            // Clear Button
            Button(
                onClick = { onApply(HistoryFilterState()) },
                modifier = Modifier.weight(1f)
            ) { Text("Clear") }
            Spacer(modifier = Modifier.width(8.dp))
            // Set Filter Button
            Button(
                onClick = { onApply(
                        HistoryFilterState(
                            locationQuery = locationText.trim(),
                            startDateMillis = startDateMillis,
                            endDateMillis = endDateMillis,
                            sortOrder = sortOrder
                        ))
                },
                modifier = Modifier.weight(1f)
            ) { Text("Apply Filter") }
        }
    }
}