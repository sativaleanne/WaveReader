package com.maciel.wavereader.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.maciel.wavereader.viewmodels.LocationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reusable location search component that handles multiple input types:
 * - City names
 * - Coordinates
 * - Zip codes
 * - Current location via GPS
 */
@Composable
fun LocationSearchField(
    locationViewModel: LocationViewModel,
    initialValue: String = "",
    label: String = "Search for a location",
    placeholder: String = "City, coordinates, or zip code",
    onLocationSelected: ((lat: Double, lon: Double, displayText: String) -> Unit)? = null,
    onTextChanged: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    showCurrentLocationButton: Boolean = true,
    showClearButton: Boolean = true,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var text by remember { mutableStateOf(initialValue) }
    var isGeocoding by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                errorMessage = null
                onTextChanged?.invoke(it)
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = enabled,
            isError = errorMessage != null,
            supportingText = {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                when {
                    isGeocoding -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    text.isNotEmpty() && showClearButton -> {
                        IconButton(
                            onClick = {
                                text = ""
                                errorMessage = null
                                onTextChanged?.invoke("")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (text.isNotBlank()) {
                        isGeocoding = true
                        errorMessage = null

                        handleLocationInput(
                            input = text.trim(),
                            context = context,
                            locationViewModel = locationViewModel,
                            onSuccess = { lat, lon, displayText ->
                                isGeocoding = false
                                // DON'T update text field - keep user's original input
                                onLocationSelected?.invoke(lat, lon, displayText)
                            },
                            onError = { error ->
                                isGeocoding = false
                                errorMessage = error
                            }
                        )
                    }
                }
            )
        )

        if (showCurrentLocationButton) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    isGeocoding = true
                    errorMessage = null

                    try {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                val lat = location.latitude
                                val lon = location.longitude

                                // Get a simple display name without updating viewModel's state
                                val displayText = "Current Location"
                                text = displayText
                                isGeocoding = false
                                onLocationSelected?.invoke(lat, lon, displayText)
                            } else {
                                isGeocoding = false
                                errorMessage = "Location not available"
                            }
                        }.addOnFailureListener {
                            isGeocoding = false
                            errorMessage = "Failed to get location"
                        }
                    } catch (e: SecurityException) {
                        isGeocoding = false
                        errorMessage = "Location permission denied"
                    }
                },
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Use current location"
                )
            }
        }
    }
}

/**
 * Smart location input handler that detects input type and processes accordingly
 */
private fun handleLocationInput(
    input: String,
    context: Context,
    locationViewModel: LocationViewModel,
    onSuccess: (lat: Double, lon: Double, displayText: String) -> Unit,
    onError: (String) -> Unit
) {
    // Check if input is coordinates (format: lat,lon or lat, lon)
    val coordPattern = "^-?\\d+\\.?\\d*\\s*,\\s*-?\\d+\\.?\\d*$".toRegex()

    when {
        coordPattern.matches(input) -> {
            // Parse as coordinates
            val parts = input.split(",").map { it.trim().toDoubleOrNull() }
            if (parts.size == 2 && parts.all { it != null }) {
                val lat = parts[0]!!
                val lon = parts[1]!!

                // Validate coordinate ranges
                if (lat in -90.0..90.0 && lon in -180.0..180.0) {
                    // Format for display
                    val displayText = formatCoordinates(lat, lon)
                    onSuccess(lat, lon, displayText)
                } else {
                    onError("Coordinates out of valid range")
                }
            } else {
                onError("Invalid coordinate format")
            }
        }

        input.matches("^\\d{5}$".toRegex()) -> {
            // US zip code - geocode it
            geocodeLocation(input, context, locationViewModel, onSuccess, onError)
        }

        else -> {
            // Treat as place name
            geocodeLocation(input, context, locationViewModel, onSuccess, onError)
        }
    }
}

/**
 * Helper function to geocode a location and handle the result
 */
private fun geocodeLocation(
    input: String,
    context: Context,
    locationViewModel: LocationViewModel,
    onSuccess: (lat: Double, lon: Double, displayText: String) -> Unit,
    onError: (String) -> Unit
) {
    // Reset error state
    locationViewModel.resetLocationState()

    // Trigger geocoding
    locationViewModel.selectLocation(input, context)

    // Use coroutine to wait for result
    CoroutineScope(Dispatchers.Main).launch {
        // Wait for geocoding to complete
        var attempts = 0
        while (attempts < 30) { // Max 3 seconds
            delay(100)
            attempts++

            val coords = locationViewModel.coordinatesState.value
            if (coords != null) {
                val displayText = locationViewModel.displayLocationText
                if (displayText != "No location selected") {
                    onSuccess(coords.first, coords.second, displayText)
                    return@launch
                }
            }

            if (locationViewModel.locationError) {
                onError("Location not found")
                return@launch
            }
        }

        // Timeout
        onError("Location search timed out")
    }
}

/**
 * Format coordinates for display
 */
private fun formatCoordinates(lat: Double, lon: Double): String {
    val latDir = if (lat >= 0) "N" else "S"
    val lonDir = if (lon >= 0) "E" else "W"
    return "%.4f°%s, %.4f°%s".format(
        kotlin.math.abs(lat), latDir,
        kotlin.math.abs(lon), lonDir
    )
}