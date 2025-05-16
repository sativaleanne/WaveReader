package com.example.wavereader.ui.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.wavereader.model.GraphDisplayOptions
import com.example.wavereader.model.MeasuredWaveData

/*
* Sensor Graph setup
* TODO: Fix Scroll to stick to far end and add forecasts
 */
@Composable
fun SensorGraph(
    waveData: List<MeasuredWaveData>,
    display: GraphDisplayOptions
) {
    if (waveData.isEmpty()) return

    // Filtered data
    val height = if (display.showHeight) waveData.map { it.waveHeight } else emptyList()
    val period = if (display.showPeriod) waveData.map { it.wavePeriod } else emptyList()
    val direction = if (display.showDirection) waveData.map { it.waveDirection } else emptyList()

    val forecastLines = mutableListOf<GraphLine>()
    val timeValues = waveData.map { it.time }.toMutableList()

    // TODO: Forecast values
    if (display.showForecast) {
        //TODO
    }

    // Limit X axis to 10
    val maxLabels = 10
    val labelStep = if (timeValues.size > maxLabels) timeValues.size / maxLabels else 1

    val timeLabels = timeValues.mapIndexed { index, time ->
        if (index % labelStep == 0 || index == timeValues.lastIndex) "${time.toInt()}s" else ""
    }

    val mainLines = listOfNotNull(
        if (height.isNotEmpty()) GraphLine(height, "Wave Height", Color.Blue, "ft") else null,
        if (period.isNotEmpty()) GraphLine(period, "Wave Period", Color.Cyan, "s") else null,
        if (direction.isNotEmpty()) GraphLine(direction, "Wave Direction", Color.Green, "°") else null
    )


    Graph(
        lines = mainLines,
        timeLabels = timeLabels,
        isInteractive = true,
        isScrollable = true
    )
}
