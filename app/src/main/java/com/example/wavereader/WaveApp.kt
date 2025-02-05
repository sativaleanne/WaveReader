package com.example.wavereader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wavereader.data.Screen
import com.example.wavereader.ui.RecordDataScreen
import com.example.wavereader.ui.SearchDataScreen
import com.example.wavereader.viewmodels.LocationViewModel
import com.example.wavereader.viewmodels.ServiceViewModel
import com.example.wavereader.viewmodels.SensorViewModel


@Composable
fun WaveApp(viewModel: SensorViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.valueOf(
        backStackEntry?.destination?.route ?: Screen.Record.name
    )
    val serviceViewModel : ServiceViewModel = viewModel(factory = ServiceViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            OptionsBar(viewModel = viewModel,
                navigateRecord = {navController.navigate(Screen.Record.name)},
                navigateSearch = {navController.navigate(Screen.Search.name)}
                )}
    ) { innerPadding ->
        NavHost(navController = navController,
                startDestination = Screen.Record.name,
            modifier = Modifier.padding(innerPadding)
            ) {
            composable(route = Screen.Splash.name) {
                //Splash Screen
            }
            composable(route = Screen.Record.name) {
                //Record Screen
                RecordDataScreen(viewModel = viewModel, uiState = uiState)
            }
            composable(route = Screen.Search.name) {
                //Search Screen
                SearchDataScreen(serviceViewModel = serviceViewModel)
            }
        }
    }
}


@Composable
fun OptionsBar(
    viewModel: SensorViewModel,
    navigateRecord: () -> Unit,
    navigateSearch: () -> Unit
) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = navigateRecord) {
                Text(text = "Record Waves")
            }
            Spacer(modifier = Modifier.padding(16.dp))
            Button(onClick = navigateSearch) {
                Text(text = "Find Waves")
            }
        }
    }
}


