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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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

    private fun applyFilters() {
        val filter = _filterState.value
        println("DEBUG: Applying filters")
        println("  - Search coordinates: ${filter.searchLatLng}")
        println("  - Radius: ${filter.radiusMiles} miles")
        println("  - Location query (display): ${filter.locationQuery}")

        viewModelScope.launch {
            _isLoading.value = true

            // Fetch all records from Firestore
            val raw = firestoreRepository.fetchHistoryRecords(
                locationQuery = "", // Fetch all
                sortDescending = filter.sortOrder == SortOrder.DATE_DESCENDING,
                startDateMillis = filter.startDateMillis,
                endDateMillis = filter.endDateMillis
            )

            println("DEBUG: Fetched ${raw.size} raw records from Firestore")

            // Apply location filtering based on coordinates and proximity
            val filtered = if (filter.searchLatLng != null) {
                raw.filter { record ->
                    if (record.lat != null && record.lon != null) {
                        val distance = calculateDistance(
                            filter.searchLatLng.first,
                            filter.searchLatLng.second,
                            record.lat,
                            record.lon
                        )
                        val withinRadius = distance <= filter.radiusMiles

                        println("DEBUG: Record '${record.location}' at (${record.lat}, ${record.lon})")
                        println("       Distance: ${distance.toInt()} miles - ${if (withinRadius) "INCLUDED" else "EXCLUDED"}")

                        withinRadius
                    } else {
                        println("DEBUG: Record '${record.location}' has no coordinates - EXCLUDED")
                        false
                    }
                }
            } else {
                println("DEBUG: No location filter applied - showing all records")
                raw
            }

            println("DEBUG: Final filtered count: ${filtered.size} records")

            _historyData.value = filtered
            _isLoading.value = false
        }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in miles
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusMiles = 3959.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusMiles * c
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

