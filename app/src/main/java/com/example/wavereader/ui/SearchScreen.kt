package com.example.wavereader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wavereader.R
import com.example.wavereader.model.WaveDataResponse
import com.example.wavereader.viewmodels.LocationViewModel
import com.example.wavereader.viewmodels.ServiceUiState
import com.example.wavereader.viewmodels.ServiceViewModel

@Composable
fun SearchDataScreen(
    locationViewModel: LocationViewModel
) {
    val serviceViewModel : ServiceViewModel = viewModel(factory = ServiceViewModel.Factory)
    // Collect coordinates from LocationViewModel
    val coordinates by locationViewModel.coordinatesState.observeAsState(Pair(0.0, 0.0))

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        ShowSearchData(serviceUiState = serviceViewModel.serviceUiState)
        Spacer(modifier = Modifier.height(16.dp))
        UserSearchField(locationViewModel)


        // Show latitude and longitude if available
        if (coordinates.first != 0.0 && coordinates.second != 0.0) {
            Text(stringResource(R.string.latitude_longitude, coordinates.first, coordinates.second))
            Spacer(modifier = Modifier.height(16.dp))

            // Button to Fetch Wave Data using Lat/Lon
            Button(onClick = { serviceViewModel.fetchWaveData(coordinates.first, coordinates.second) }) {
                Text(stringResource(R.string.fetch_wave_data_button))
            }
        }
    }
}

@Composable
fun UserSearchField(
    locationViewModel: LocationViewModel
) {
    val callBack = {locationViewModel.fetchCoordinates(locationViewModel.zipCode)}
    OutlinedTextField(
        value = locationViewModel.zipCode,
        onValueChange = { input -> locationViewModel.updateZipCode(input) },
        label = { Text(stringResource(R.string.enter_zip_code_label)) },
        leadingIcon = {
            if (locationViewModel.zipCode.isNotEmpty()) {
                IconButton(onClick = { callBack() }) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search_icon_descr))
                }
            }
        },
        trailingIcon = {
            if (locationViewModel.zipCode.isNotEmpty()) {
                IconButton(onClick = { locationViewModel.updateZipCode("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear_text_descr))
                }
            }
        },
        isError = locationViewModel.zipCodeHasErrors,
        supportingText = {
            if (locationViewModel.zipCodeHasErrors) {
                Text(stringResource(R.string.zip_code_error))
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {callBack()}
        ),
        modifier = Modifier.fillMaxWidth()
    )
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
            contentDescription = stringResource(R.string.error_image_descr)
        )
        Text(text = stringResource(R.string.loading_failed_text), modifier = Modifier.padding(16.dp))
    }
}

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
            Column {
                Text("Wave Height: ${it.current.waveHeight} feet")
                Text("Wave Period: ${it.current.wavePeriod} seconds")
                Text("Wave Direction: ${it.current.waveDirection} degrees")
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth()
                ) {
                    DrawServiceGraph(it.hourly)
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

