package com.maciel.wavereader.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.maciel.wavereader.data.FirestoreRepository
import com.maciel.wavereader.model.HistoryFilterState
import com.maciel.wavereader.model.HistoryRecord
import com.maciel.wavereader.model.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// TODO: Add Location View Model for location searching by zip and lat/long

class HistoryViewModel(
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {

    private val _historyData = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val historyData: StateFlow<List<HistoryRecord>> = _historyData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _expandedItems = MutableStateFlow<Set<String>>(emptySet())
    val expandedItems: StateFlow<Set<String>> = _expandedItems.asStateFlow()

    //Select Items
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode = _isSelectionMode.asStateFlow()

    private val _selectedItems = MutableStateFlow<Set<String>>(emptySet())
    val selectedItems = _selectedItems.asStateFlow()

    fun enableSelectionMode(itemId: String) {
        _isSelectionMode.value = true
        _selectedItems.value = setOf(itemId)
    }

    fun toggleItemSelection(itemId: String) {
        _selectedItems.value = if (_selectedItems.value.contains(itemId)) {
            _selectedItems.value - itemId
        } else {
            _selectedItems.value + itemId
        }
    }

    fun clearSelection() {
        _isSelectionMode.value = false
        _selectedItems.value = emptySet()
    }

    fun deleteSelectedItems() {
        val itemsToDelete = _selectedItems.value.toSet()

        _historyData.value = _historyData.value.filterNot {
            itemsToDelete.contains(it.id)
        }
        clearSelection()

        viewModelScope.launch {
            try {
                for (id in itemsToDelete) {
                    firestoreRepository.deleteHistoryRecord(id)
                }
            } catch (e: Exception) {
                // Revert on failure
                _errorMessage.value = "Delete failed"
                applyFilters()  // Refresh to restore data
            }
        }
    }

    // Filter Items
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

            val raw = firestoreRepository.fetchHistoryRecords(
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

    companion object {
        fun provideFactory(
            firestoreRepository: FirestoreRepository
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HistoryViewModel(firestoreRepository)
            }
        }
    }
}
