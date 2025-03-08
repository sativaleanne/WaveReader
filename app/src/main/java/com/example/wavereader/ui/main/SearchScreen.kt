package com.example.wavereader.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.wavereader.R
import com.example.wavereader.model.WaveDataResponse
import com.example.wavereader.ui.DrawServiceGraph
import com.example.wavereader.viewmodels.LocationViewModel
import com.example.wavereader.viewmodels.ServiceViewModel
import com.example.wavereader.viewmodels.UiState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

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
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var isMapExpanded by remember { mutableStateOf(false) }
    val shouldFetch = remember { mutableStateOf(false) }

    // Get data from API
    LaunchedEffect(shouldFetch.value) {
        if (shouldFetch.value) {
            coordinates?.let { serviceViewModel.fetchWaveData(it) }
            shouldFetch.value = false
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Search bar
        //TODO: VALIDATION
        SearchForLocation(
            locationViewModel = locationViewModel,
            fusedLocationClient = fusedLocationClient
        )

        // Display Location info
        Text(
            text = "Location: ${locationViewModel.displayLocationText}",
            style = MaterialTheme.typography.labelMedium
        )

        // Expandable Map Display Card
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

        // Search + Filters
        //TODO: ADD FILTERING
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { shouldFetch.value = true }) {
                Text("Search")
            }
            OutlinedButton(onClick = { /* TODO: open filters */ }) {
                Text("Filters")
            }
        }
        ShowSearchData(serviceViewModel.serviceUiState)
    }
}

// Search Field for getting location and displaying on map
@Composable
fun SearchForLocation(
    locationViewModel: LocationViewModel,
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Search for a place") },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (text.isNotBlank()) {
                        locationViewModel.selectLocation(text, context)
                    }
                }
            )
        )
        // Find Current Location
        IconButton(
            onClick = {
                locationViewModel.fetchUserLocation(context, fusedLocationClient)
            }
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Use current location"
            )
        }
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
            waveData = (serviceUiState as UiState.Success<WaveDataResponse>).data
        )
        is UiState.Error -> ErrorScreen()
    }
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
            if(it.current.waveHeight == null){
                Text(text = "There is no wave data at this location!",
                    fontWeight = FontWeight.Bold
                )
            }
            Column {
                Text("Wave Height: ${it.current.waveHeight} feet")
                Text("Wave Period: ${it.current.wavePeriod} seconds")
                Text("Wave Direction: ${it.current.waveDirection} degrees")
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) {
                    if (it.hourly.time.isEmpty()) {
                        // Show a loading indicator or placeholder for the graph area
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        DrawServiceGraph(it.hourly)
                    }
                }
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


