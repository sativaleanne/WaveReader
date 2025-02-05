package com.example.wavereader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wavereader.R
import com.example.wavereader.viewmodels.ServiceUiState
import com.example.wavereader.viewmodels.ServiceViewModel
import com.example.wavereader.model.WaveDataResponse
import com.example.wavereader.viewmodels.LocationViewModel

@Composable
fun SearchDataScreen(
    serviceViewModel: ServiceViewModel
) {
    val locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
    var zip by remember { mutableStateOf("") }

    // Collect coordinates from LocationViewModel
    val coordinates by locationViewModel.coordinatesState.observeAsState(Pair(0.0, 0.0))

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShowSearchData(serviceUiState = serviceViewModel.serviceUiState)

        // Enter Zip Code
        OutlinedTextField(
            value = zip,
            onValueChange = { zip = it },
            label = { Text("Enter Zip Code") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Button to Fetch Location (Lat/Lon)
        Button(onClick = { locationViewModel.fetchCoordinates(zip) }) {
            Text("Get Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show latitude and longitude if available
        if (coordinates.first != 0.0 && coordinates.second != 0.0) {
            Text("Lat: ${coordinates.first}, Lon: ${coordinates.second}")
            Spacer(modifier = Modifier.height(16.dp))

            // Button to Fetch Wave Data using Lat/Lon
            Button(onClick = { serviceViewModel.fetchWaveData(coordinates.first, coordinates.second) }) {
                Text("Fetch Wave Data")
            }
        }
    }
}

@Composable
fun ShowSearchData(
    serviceUiState: ServiceUiState
) {
    when (serviceUiState) {
        is ServiceUiState.Loading -> LoadingScreen(modifier = Modifier)
        is ServiceUiState.Success -> SearchResultScreen(
            serviceUiState.waveData,
            modifier = Modifier
        )
        is ServiceUiState.Error -> ErrorScreen(modifier = Modifier)
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
            contentDescription = "Error"
        )
        Text(text = "Loading Failed", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun SearchResultScreen(
    waveData: WaveDataResponse,
    modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        waveData.let {
            Column {
                Text("Wave Height: ${it.current.waveHeight} feet")
                Text("Wave Period: ${it.current.wavePeriod} seconds")
                Text("Wave Direction: ${it.current.waveDirection} degrees")
                DrawServiceGraph(it.hourly)
            }
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = "Loading..."
    )
}

