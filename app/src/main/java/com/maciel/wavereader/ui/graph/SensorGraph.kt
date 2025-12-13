package com.maciel.wavereader.ui.graph

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.maciel.wavereader.model.GraphDisplayOptions
import com.maciel.wavereader.model.MeasuredWaveData
import com.maciel.wavereader.utils.predictNextBigWave
import java.util.Locale

/*
* Sensor Graph setup
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

    val forecastIndex = if (display.showForecast && predictNextBigWave(waveData)) {
        waveData.lastIndex
    } else {
        -1
    }

    // Limit X axis to 10
    val maxLabels = 10
    val labelStep = if (timeValues.size > maxLabels) timeValues.size / maxLabels else 1

    val timeLabels = timeValues.mapIndexed { index, time ->
        if (index % labelStep == 0 || index == timeValues.lastIndex) String.format(Locale.US, "%.1f s", time) else ""
    }

    val mainLines = listOfNotNull(
        if (height.isNotEmpty()) GraphLine(height, "Wave Height", Color.Blue, "ft") else null,
        if (period.isNotEmpty()) GraphLine(period, "Wave Period", Color.hsl(180F, 1F, 0.27F), "s") else null,
        if (direction.isNotEmpty()) GraphLine(direction, "Wave Direction", Color.hsl(137F, 0.52F, 0.33F), "°") else null
    )


    Graph(
        lines = mainLines,
        timeLabels = timeLabels,
        isInteractive = true,
        isScrollable = true,
        forecastIndex = forecastIndex
    )
}

@Composable
fun ColorCheck(){
    Column {
        Text(text = "Color Check", color = Color.Blue)
        Text(text = "Color Check", color = Color.hsl(180F, 1F, 0.27F))
        Text(text = "Color Check", color = Color.hsl(137F, 0.52F, 0.33F))
    }
}

@Preview
@Composable
fun ColorCheckPreview() {
    ColorCheck()
}
