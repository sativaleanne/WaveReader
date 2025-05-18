package com.example.wavereader.ui.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.wavereader.model.MeasuredWaveData

/*
* History graph setup for drop down mini graphs in history page.
* TODO: Fix X axis labels for summary graphs
 */
@Composable
fun HistoryGraph(waveData: List<MeasuredWaveData>, isInteractive: Boolean = false, isXLabeled: Boolean = true) {
    if (waveData.isEmpty()) return

    val height = waveData.map { it.waveHeight }
    val period = waveData.map { it.wavePeriod }
    val direction = waveData.map { it.waveDirection }
    val timeLabels = waveData.map { "${it.time.toInt()}s" }

    val lines = listOf(
        GraphLine(height, "Wave Height", Color.Blue, "ft"),
        GraphLine(period, "Wave Period", Color.Cyan, "s"),
        GraphLine(direction, "Wave Direction", Color.Green, "°")
    )

    Graph(lines, timeLabels, isInteractive = isInteractive, isXLabeled = isXLabeled)
}
