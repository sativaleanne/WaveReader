package com.maciel.wavereader.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maciel.wavereader.data.HistoryRepository
import com.maciel.wavereader.model.HistoryFilterState
import com.maciel.wavereader.model.HistoryRecord
import com.maciel.wavereader.model.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// TODO: Add Location View Model for location searching by zip and lat/long

class HistoryViewModel(
    private val historyRepository: HistoryRepository = HistoryRepository()
) : ViewModel() {

    private val _historyData = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val historyData: StateFlow<List<HistoryRecord>> = _historyData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _expandedItems = MutableStateFlow<Set<String>>(emptySet())
    val expandedItems: StateFlow<Set<String>> = _expandedItems.asStateFlow()

    private val _filterState = MutableStateFlow(HistoryFilterState())
    val filterState: StateFlow<HistoryFilterState> = _filterState.asStateFlow()

    fun updateFilter(newFilter: HistoryFilterState) {
        _filterState.value = newFilter
        applyFilters()
    }

    // Apply new filters from user selection update.
    private fun applyFilters() {
        val filter = _filterState.value
        viewModelScope.launch {
            _isLoading.value = true

            val raw = historyRepository.fetchHistoryRecords(
                locationQuery = filter.locationQuery,
                sortDescending = filter.sortOrder == SortOrder.DATE_DESCENDING,
                startDateMillis = filter.startDateMillis,
                endDateMillis = filter.endDateMillis
            )

            val filtered = raw.filter {
                it.location.contains(filter.locationQuery, ignoreCase = true)
            }

            _historyData.value = filtered
            _isLoading.value = false
        }
    }

    // Expand data details
    fun toggleItemExpansion(id: String) {
        _expandedItems.value = if (_expandedItems.value.contains(id)) {
            _expandedItems.value - id
        } else {
            _expandedItems.value + id
        }
    }

    // Sort by most recent by default
    fun setDefaultRecentFilter() {
        val defaultFilter = HistoryFilterState(
            sortOrder = SortOrder.DATE_DESCENDING,
            startDateMillis = null,
            endDateMillis = null
        )
        updateFilter(defaultFilter)
    }
}
