package com.example.wavereader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wavereader.data.Screen


@Composable
fun WaveApp(viewModel: WaveViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.valueOf(
        backStackEntry?.destination?.route ?: Screen.Record.name
    )
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
                RecordDataScreen(viewModel = viewModel)
            }
            composable(route = Screen.Search.name) {
                //Search Screen
                SearchDataScreen(viewModel = viewModel)
            }
        }
    }
}


@Composable
fun OptionsBar(
    viewModel: WaveViewModel,
    navigateRecord: () -> Unit,
    navigateSearch: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier,

        actions = {
            Button(onClick = navigateRecord) {
                Text(text = "Record Waves")
            }
            Button(onClick = navigateSearch) {
                Text(text = "Find Waves")
            }
        }
    )
}

@Composable
fun RecordDataScreen(viewModel: WaveViewModel) {
    var isSensorActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Toggle Button
        Button(
            modifier = Modifier.padding(16.dp)
                .align(Alignment.CenterHorizontally),
            onClick = {
                isSensorActive = !isSensorActive
                if (isSensorActive) {
                    viewModel.startSensors()
                } else {
                    viewModel.stopSensors()
                }
            }
        ) {
            Text(text = if (isSensorActive) "Pause Sensors" else "Resume Sensors")
        }
    }

}

@Composable
fun SearchDataScreen(viewModel: WaveViewModel) {
    var zip = rememberSaveable { mutableStateOf("zip")}
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = viewModel.tiltX.toString(),
            onValueChange = {},
        )
    }
}

@Composable
fun ShowData(
    tiltX: Float,
    tiltY: Float,
    tiltZ: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tilt X
        Text(
            text = "Tilt X: %.2f°".format(tiltX),
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        // Tilt Y
        Text(
            text = "Tilt Y: %.2f°".format(tiltY),
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        // Tilt Z
        Text(
            text = "Tilt Z: %.2f°".format(tiltZ),
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}
