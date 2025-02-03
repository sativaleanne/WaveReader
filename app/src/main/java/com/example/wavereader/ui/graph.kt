package com.example.wavereader.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wavereader.model.Hourly
import com.example.wavereader.model.MeasuredWaveData
import com.example.wavereader.testData.fakeWaveData


@Composable
fun DrawServiceGraph(waveData: Hourly) {
    Box(
        modifier = Modifier.size(width = 600.dp, height = 300.dp)
            .background(color = Color.White)
            .fillMaxSize()
    ) {
        Graph(waveData.waveHeight, color = Color.Blue)
        Graph(waveData.wavePeriod, color = Color.Green)
        Graph(waveData.waveDirection, color = Color.Magenta)
    }
}

@Composable
fun DrawSensorGraph(waveData: List<MeasuredWaveData>) {
    val waveHeight = emptyList<Float>().toMutableList()
    val wavePeriod = emptyList<Float>().toMutableList()
    val waveDirection = emptyList<Float>().toMutableList()

    for (i in waveData.indices) {
        waveHeight += waveData[i].waveHeight
        wavePeriod += waveData[i].wavePeriod
        waveDirection += waveData[i].waveDirection
    }
    Box(
        modifier = Modifier.size(width = 600.dp, height = 300.dp)
            .background(color = Color.White)
            .fillMaxSize()
    ) {
        Graph(waveHeight, color = Color.Blue)
        Graph(wavePeriod, color = Color.Green)
        Graph(waveDirection, color = Color.Magenta)
    }
}

@Composable
fun Graph(waveData: List<Float>, color: Color){
    Canvas(
        modifier = Modifier.padding(8.dp)
            .aspectRatio(2f)
            .fillMaxSize()
    ) {
        //Draw Graph Boarder
        val barWidthPx = 0.5.dp.toPx()
        drawRect(Color.Black, style = Stroke(barWidthPx))

        //Draw horizontal Graph line
        val horizontalLines = 3
        val horizontalSize = size.height / (horizontalLines + 1)
        repeat(horizontalLines) { i ->
            val startY = horizontalSize * (i + 1)
            drawLine(
                Color.Black,
                start = Offset(0f, startY),
                end = Offset(size.width, startY),
                strokeWidth = barWidthPx
            )
        }
        val maxPoint = (waveData.maxOrNull() ?: 1f)
        val minPoint = (waveData.minOrNull() ?: 0f)

        val graphWidth = size.width
        val graphHeight = size.height

        val xStep = graphWidth / (waveData.size - 1).coerceAtLeast(1)

        val normalizedHeights = waveData.map { height ->
            ((height - minPoint) / (maxPoint - minPoint + 0.01)) * graphHeight
        }

        for (i in 1 until normalizedHeights.size) {
            drawLine(
                color = color,
                start = Offset(
                    (i - 1) * xStep,
                    (graphHeight - normalizedHeights[i - 1]).toFloat()
                ),
                end = Offset(i * xStep, (graphHeight - normalizedHeights[i]).toFloat()),
                strokeWidth = 4f
            )
        }
    }
}




@Preview
@Composable
fun PreviewDrawGraph(
){
    //WavePeriodGraph(fakeWaveData)
    DrawServiceGraph(fakeWaveData)
}