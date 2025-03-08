package com.example.wavereader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wavereader.data.Screen
import com.example.wavereader.ui.RecordDataScreen
import com.example.wavereader.ui.SearchDataScreen
import com.example.wavereader.viewmodels.LocationViewModel
import com.example.wavereader.viewmodels.SensorViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaveApp(viewModel: SensorViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.valueOf(
        backStackEntry?.destination?.route ?: Screen.Record.name)
    val locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()

    val tabs = listOf(Screen.Record, Screen.Search)
    val selectedTabIndex = rememberSaveable { mutableIntStateOf(tabs.indexOf(currentScreen)) }

    val showInfoCard = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text(
                        text = stringResource(R.string.titlename)
                    ) },
                    actions = {
                        IconButton(onClick = { showInfoCard.value = true }) {
                            Icon(Icons.Default.Info, contentDescription = stringResource(R.string.informationbuttondescr))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex.intValue,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    tabs.forEachIndexed { index, screen ->
                        Tab(
                            selected = selectedTabIndex.intValue == index,
                            onClick = {
                                selectedTabIndex.intValue = index
                                navController.navigate(screen.name)
                            },
                            text = { Text(screen.name) },
                            icon = {
                                Icon(
                                    imageVector = if (screen == Screen.Record) Icons.Default.PlayArrow else Icons.Default.Search,
                                    contentDescription = screen.name
                                )
                            },
                            selectedContentColor = Color.DarkGray,
                            unselectedContentColor = Color.LightGray
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (showInfoCard.value) {
            ShowInfoDialog(showInfoCard = showInfoCard)
        }
        NavHost(navController = navController,
                startDestination = Screen.Record.name,
            modifier = Modifier.padding(innerPadding)
            ) {
            composable(route = Screen.Record.name) {
                //Record Screen
                RecordDataScreen(viewModel = viewModel, uiState = uiState)
            }
            composable(route = Screen.Search.name) {
                //Search Screen
                SearchDataScreen(locationViewModel)
            }
        }
    }
}

@Composable
fun ShowInfoDialog(showInfoCard: MutableState<Boolean>) {
    Dialog(onDismissRequest = {showInfoCard.value = false}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier
                .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.info_sensor_header),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
                Text(
                    text = stringResource(R.string.info_sensor_body),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.calculating_wave_height_title),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
                Text(
                    text = stringResource(R.string.calculating_wave_height_body),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.calculating_wave_period_title),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
                Text(
                    text = stringResource(R.string.calculating_wave_period_body),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.calculating_wave_direction_title),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
                Text(
                    text = stringResource(R.string.calculating_wave_direction_body),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.info_api_header),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
                Text(
                    text = stringResource(R.string.info_api_body),
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
                TextButton(
                    onClick = { showInfoCard.value = false },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text(stringResource(R.string.dismiss_text))
                }
            }
        }
    }
}





