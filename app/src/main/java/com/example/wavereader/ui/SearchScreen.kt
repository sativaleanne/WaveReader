package com.example.wavereader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.wavereader.R
import com.example.wavereader.viewmodels.ServiceUiState
import com.example.wavereader.viewmodels.ServiceViewModel
import com.example.wavereader.model.WaveDataResponse

@Composable
fun SearchDataScreen(
    serviceViewModel: ServiceViewModel
) {
    var zip = rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShowSearchData(
            serviceUiState = serviceViewModel.serviceUiState
            )
        // Enter Zip Code
        OutlinedTextField(
            value = zip.value,
            onValueChange = { newZip ->
                zip.value = newZip
            },
            label = { Text("Enter Zip Code") },
            modifier = Modifier
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { serviceViewModel.fetchWaveData(zip.value)}) {
            Text("Get Wave Data")
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

