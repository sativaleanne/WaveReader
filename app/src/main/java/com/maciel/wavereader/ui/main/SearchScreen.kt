package com.maciel.wavereader.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import com.maciel.wavereader.R
import com.maciel.wavereader.model.ApiVariable
import com.maciel.wavereader.model.FilterPreset
import com.maciel.wavereader.model.WaveApiQuery
import com.maciel.wavereader.model.WaveDataResponse
import com.maciel.wavereader.ui.components.LocationSearchField
import com.maciel.wavereader.ui.components.WaveDataCard
import com.maciel.wavereader.ui.graph.ServiceGraph
import com.maciel.wavereader.viewmodels.LocationViewModel
import com.maciel.wavereader.viewmodels.ServiceViewModel
import com.maciel.wavereader.viewmodels.UiState

/*
* Search Screen for retrieving wave data through zip code or map location
*
* Resources: https://medium.com/@karollismarmokas/integrating-google-maps-in-android-with-jetpack-compose-user-location-and-search-bar-a432c9074349
 */
@Composable
fun SearchDataScreen(
    locationViewModel: LocationViewModel,
    navController: NavHostController
) {
    val serviceViewModel: ServiceViewModel = viewModel(factory = ServiceViewModel.Factory)
    val coordinates by locationViewModel.coordinatesState.observeAsState()
    val isSearching = serviceViewModel.isSearching
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var isMapExpanded by remember { mutableStateOf(false) }

    var selectedPreset by remember { mutableStateOf(FilterPreset.Wave) }
    var selectedVariables by remember { mutableStateOf(selectedPreset.variables) }


    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Location Search Component
        LocationSearchField(
            locationViewModel = locationViewModel,
            label = "Search for a location",
            placeholder = "City, coordinates, or zip code",
            onLocationSelected = { lat, lon, displayText ->
                // Location automatically updates in LocationViewModel
                println("Selected: $displayText at ($lat, $lon)")
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Display Location info
        Text(
            text = "Location: ${locationViewModel.displayLocationText}",
            style = MaterialTheme.typography.labelMedium
        )

        // Expandable Map Display Card
        // Separate into composable
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isMapExpanded = !isMapExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMapExpanded) "Tap to Collapse Map" else "Tap to Expand Map",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Icon(
                        imageVector = if (isMapExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Button"
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isMapExpanded) 320.dp else 120.dp)
                        .padding(top = 8.dp)
                ) {
                    MapScreen(locationViewModel, fusedLocationClient)
                }
            }
        }

//        // Search + Filters Button later
//        DropDownFilterSearchPresets(
//            selectedPreset = selectedPreset,
//            onPresetSelected = {
//                selectedPreset = it
//                selectedVariables = it.variables
//            }
//        )

        SearchButton(
            coordinates = coordinates,
            isSearching = isSearching,
            onClick = {
                coordinates?.let { (lat, lon) ->
                    val query = WaveApiQuery(
                        latitude = lat,
                        longitude = lon,
                        variables = selectedVariables.ifEmpty {
                            setOf(ApiVariable.WaveHeight, ApiVariable.WaveDirection, ApiVariable.WavePeriod)
                        },
                        forecastDays = 1
                    )
                    serviceViewModel.fetchWaveData(query)
                }
            }
        )
        // Display Data or current state of searching data
        ShowSearchData(serviceViewModel.serviceUiState)
    }
}

@Composable
fun SearchButton(
    coordinates: Pair<Double, Double>?,
    isSearching: Boolean,
    onClick: () -> Unit
) {
    val isEnabled = coordinates != null && !isSearching

    Button(
        onClick = onClick,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
        enabled = isEnabled,
        modifier = Modifier.fillMaxWidth(0.6f) // Make it a bit wider for better visibility
    ) {
        if (isSearching) {
            // Show loading indicator while searching
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Searching...")
            }
        } else {
            // Show search text with icon
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search Wave Data")
            }
        }
    }

    // Show helper text when disabled
    if (!isEnabled && !isSearching) {
        Text(
            text = "Please select a location to search",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Displaying Current state of searching data
@Composable
fun ShowSearchData(
    serviceUiState: UiState<WaveDataResponse>
) {
    when (serviceUiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Success -> SearchResultScreen(
            waveData = serviceUiState.data
        )
        is UiState.Error -> ErrorScreen()
    }
}

// Show text and graph of data response from API
@Composable
fun SearchResultScreen(
    waveData: WaveDataResponse,
    modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        waveData.let {
            if(it.current?.waveHeight == null){
                Text(text = "There is no wave data at this location!",
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WaveDataCard(
                        "Current Conditions",
                        listOf(it.current?.waveHeight, it.current?.wavePeriod, it.current?.waveDirection),
                        listOf("Height", "Period", "Direction"),
                        listOf("ft", "s", "°")
                    )
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    val hourly = waveData.hourly
                    if (hourly?.time?.isNotEmpty() == true) {
                        ServiceGraph(hourly)
                    } else {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Text("No graph data available.")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading_image_descr)
    )
}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error),
            contentDescription = stringResource(R.string.error_image_descr)
        )
        Text(text = stringResource(R.string.loading_failed_text), modifier = Modifier.padding(16.dp))
    }
}


