package com.example.wavereader.ui.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.wavereader.model.Hourly

/*
* Service Graph setup
 */
@Composable
fun ServiceGraph(waveData: Hourly) {
    val timeLabels = waveData.time.map { formatTime(it) }

    val lines = listOfNotNull(
        waveData.waveHeight?.let { GraphLine(it.filterNotNull(), "Wave Height", Color.Blue, "ft") },
        waveData.wavePeriod?.let { GraphLine(it.filterNotNull(), "Wave Period", Color.Cyan, "s") },
        waveData.waveDirection?.let { GraphLine(it.filterNotNull(), "Wave Direction", Color.Green, "°") },
        waveData.windWaveHeight?.let { GraphLine(it.filterNotNull(), "Wind Height", Color.Red, "ft") },
        waveData.windWavePeriod?.let { GraphLine(it.filterNotNull(), "Wind Period", Color.Magenta, "s") },
        waveData.windWaveDirection?.let { GraphLine(it.filterNotNull(), "Wind Direction", Color.Black, "°") },
        waveData.swellWaveHeight?.let { GraphLine(it.filterNotNull(), "Swell Height", Color.Yellow, "ft") },
        waveData.swellWavePeriod?.let { GraphLine(it.filterNotNull(), "Swell Period", Color.LightGray, "s") },
        waveData.swellWaveDirection?.let { GraphLine(it.filterNotNull(), "Swell Direction", Color.DarkGray, "°") }
    )

    Graph(lines, timeLabels, isInteractive = true)
}
