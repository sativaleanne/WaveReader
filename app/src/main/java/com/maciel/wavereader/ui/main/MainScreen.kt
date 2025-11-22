package com.maciel.wavereader.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maciel.wavereader.R
import com.maciel.wavereader.viewmodels.LocationViewModel
import com.maciel.wavereader.viewmodels.SensorViewModel

enum class MainScreenTab(val label: String) {
    Measure("Measure Waves"),
    Search("Search Waves")
}

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
    onInfoNavigate: () -> Unit,
    isGuest: Boolean
) {
    val navController = rememberNavController()
    val locationViewModel: LocationViewModel = viewModel(factory = LocationViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route?.let { route ->
        MainScreenTab.entries.find { it.name == route }
    } ?: MainScreenTab.Measure // Default to Record

    //TODO: CHange to use MainScreenTab
    val tabs = listOf(MainScreenTab.Measure, MainScreenTab.Search)
    val selectedTabIndex = rememberSaveable { mutableIntStateOf(tabs.indexOf(currentScreen)) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = stringResource(R.string.titlename))
                    },
                    actions = {
                        DropDownMenuButton( isGuest, onInfoNavigate, onHistoryNavigate, onSignOut)
                    }
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex.intValue,
                    modifier = Modifier.fillMaxWidth()
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
                            text = { Text(screen.label) },
                            icon = {
                                Icon(
                                    imageVector = if (screen == MainScreenTab.Measure) Icons.Default.PlayArrow else Icons.Default.Search,
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
        NavHost(
            navController = navController,
            startDestination = MainScreenTab.Measure.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainScreenTab.Measure.name) {
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
    onInfoNavigate: () -> Unit,
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
                    onClick = {
                        expanded = false // Collapse the menu first
                        onHistoryNavigate()
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("About") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.informationbuttondescr)) },
                onClick = {
                    expanded = false // Collapse the menu first
                    onInfoNavigate()
                }
            )
            DropdownMenuItem(
                text = { Text("Sign In/Out") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
                onClick = {
                    expanded = false // Collapse the menu first
                    onSignOut()
                }
            )
        }
    }
}
