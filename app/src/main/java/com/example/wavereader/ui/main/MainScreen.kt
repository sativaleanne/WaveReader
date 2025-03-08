package com.example.wavereader.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.wavereader.R
import com.example.wavereader.viewmodels.LocationViewModel
import com.example.wavereader.viewmodels.SensorViewModel

enum class MainScreenTab { Record, Search }

/*
* Main Screen controls the main apps navigation within the structured Scaffolding
* with top app bar and tabs between screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SensorViewModel,
    onSignOut: () -> Unit,
    onHistoryNavigate: () -> Unit,
    isGuest: Boolean
) {
    val navController = rememberNavController()
    val locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route?.let { route ->
        MainScreenTab.entries.find { it.name == route }
    } ?: MainScreenTab.Record // Default to Record

    //TODO: CHange to use MainScreenTab
    val tabs = listOf(MainScreenTab.Record, MainScreenTab.Search)
    val selectedTabIndex = rememberSaveable { mutableIntStateOf(tabs.indexOf(currentScreen)) }

    val showInfoCard = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = stringResource(R.string.titlename))
                    },
                    actions = {
                        DropDownMenuButton( isGuest, showInfoCard, onHistoryNavigate, onSignOut)
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
                                navController.navigate(screen.name) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            text = { Text(screen.name) },
                            icon = {
                                Icon(
                                    imageVector = if (screen == MainScreenTab.Record) Icons.Default.PlayArrow else Icons.Default.Search,
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
        NavHost(
            navController = navController,
            startDestination = MainScreenTab.Record.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainScreenTab.Record.name) {
                RecordDataScreen(viewModel = viewModel, uiState = uiState, isGuest)
            }
            composable(MainScreenTab.Search.name) {
                SearchDataScreen(locationViewModel, navController)
            }
        }
    }
}

@Composable
fun DropDownMenuButton(
    isGuest: Boolean,
    showInfoCard: MutableState<Boolean>,
    onHistoryNavigate: () -> Unit,
    onSignOut: () -> Unit
    ) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (!isGuest) {
                DropdownMenuItem(
                    text = { Text("History") },
                    leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                    onClick = onHistoryNavigate
                )
            }
            DropdownMenuItem(
                text = { Text("About") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.informationbuttondescr)) },
                onClick = {
                    showInfoCard.value = true
                }
            )
            DropdownMenuItem(
                text = { Text("Sign In/Out") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                onClick = onSignOut
            )
        }
    }
}



// Info on App use, sensors, and calculations
//TODO: UPDATE
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