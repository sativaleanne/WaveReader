package com.maciel.wavereader.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.maciel.wavereader.R
import com.maciel.wavereader.ui.components.ExpandableInfoCard

@Composable
fun InfoScreen(navController: NavHostController) {
    val context = LocalContext.current
    BackHandler { navController.popBackStack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Text("About WaveReader", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sensor Use and Setup",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row {
                    Text("• ", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(R.string.info_sensor_body),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExpandableInfoCard(
                    title = "Calculating Wave Height",
                    bulletPoints = listOf(stringResource(R.string.calculating_wave_height_body))
                )
                ExpandableInfoCard(
                    title = "Calculating Wave Period",
                    bulletPoints = listOf(stringResource(R.string.calculating_wave_period_body))
                )
                ExpandableInfoCard(
                    title = "Calculating Wave Direction",
                    bulletPoints = listOf(stringResource(R.string.calculating_wave_direction_body))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "About the API",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row {
                    Text("• ", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        stringResource(R.string.info_api_body),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

