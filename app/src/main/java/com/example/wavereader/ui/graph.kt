package com.example.wavereader.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wavereader.model.Hourly
import com.example.wavereader.model.MeasuredWaveData
import com.example.wavereader.testData.fakeWaveData
import java.time.LocalDateTime

/*
* All Graph functions
 */

/*
* Service Graph setup
* TODO: Adjust for added filters
 */
@Composable
fun DrawServiceGraph(waveData: Hourly) {
    val timeLabels = waveData.time.map { formatTime(it) }
    Graph(
        waveData.waveHeight.filterNotNull(),
        waveData.wavePeriod.filterNotNull(),
        waveData.waveDirection.filterNotNull(),
        timeLabels
    )
}

/*
* Sensor Graph setup
* TODO: Adjust for added filters
 */
@Composable
fun DrawSensorGraph(waveData: List<MeasuredWaveData>) {
    if (waveData.isEmpty()) return

    val waveHeight = mutableListOf<Float>()
    val wavePeriod = mutableListOf<Float>()
    val waveDirection = mutableListOf<Float>()
    val timeLabels = mutableListOf<String>()
    val timeValues = mutableListOf<Float>()

    getLastTwenty(waveData).forEach { data ->
        waveHeight += data.waveHeight
        wavePeriod += data.wavePeriod
        waveDirection += data.waveDirection
        timeValues += data.time
    }

    val totalPoints = timeValues.size
    val maxLabels = 10
    val step = if (totalPoints > maxLabels) totalPoints / maxLabels else 1

    timeValues.forEachIndexed { index, time ->
        timeLabels += if (index % step == 0 || index == totalPoints - 1) "${time.toInt()}s" else ""
    }

    Graph(waveHeight, wavePeriod, waveDirection, timeLabels)
}

/*
* History graph setup for drop down mini graphs in history page.
* TODO: Remove graph interaction on these.
 */
@Composable
fun DrawHistoryGraph(waveData: List<MeasuredWaveData>) {
    if (waveData.isEmpty()) return

    val waveHeight = mutableListOf<Float>()
    val wavePeriod = mutableListOf<Float>()
    val waveDirection = mutableListOf<Float>()
    val timeLabels = mutableListOf<String>()
    val timeValues = mutableListOf<Float>()

    waveData.forEach { data ->
        waveHeight += data.waveHeight
        wavePeriod += data.wavePeriod
        waveDirection += data.waveDirection
        timeValues += data.time
    }

    val totalPoints = timeValues.size
    val maxLabels = 10
    val step = if (totalPoints > maxLabels) totalPoints / maxLabels else 1

    timeValues.forEachIndexed { index, time ->
        timeLabels += if (index % step == 0 || index == totalPoints - 1) {
            "${time.toInt()}s"
        } else {
            ""
        }
    }

    Graph(waveHeight, wavePeriod, waveDirection, timeLabels, isInteractive = false)
}

fun getLastTwenty(data: List<MeasuredWaveData>): List<MeasuredWaveData>{
    return if (data.size > 20){
        data.takeLast(20)
    } else
        data
}

// Formats API time data from ISO to hour.
fun formatTime(time: String): String {
    return LocalDateTime.parse(time).hour.toString()
}

/*
* Graph function
* TODO: Add check for if interaction is allowed.
*  TODO: Smooth graph lines.
*   TODO: Potential scroll back through graph?
 */
@Composable
fun Graph(
    waveHeight: List<Float>,
    wavePeriod: List<Float>,
    waveDirection: List<Float>,
    timeLabels: List<String>,
    isInteractive: Boolean = true
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(Size.Zero) } // Store canvas size

    val minScale = 1f
    val maxScale = 5f


    // Remember the initial offset
    var initialOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    Box(
        modifier = Modifier
            .height(300.dp)
            .clip(RectangleShape)
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .then(
                if (isInteractive) Modifier.pointerInput(Unit) {
                    // Zoom and Pan
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = scale * zoom
                        scale = newScale.coerceIn(minScale, maxScale)

                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val offsetXChange = (centerX - offset.x) * (newScale / scale - 1)
                        val offsetYChange = (centerY - offset.y) * (newScale / scale - 1)

                        val maxOffsetX = (size.width / 2) * (scale - 1)
                        val minOffsetX = -maxOffsetX
                        val maxOffsetY = (size.height / 2) * (scale - 1)
                        val minOffsetY = -maxOffsetY

                        if (scale * zoom <= maxScale) {
                            offset = Offset(
                                (offset.x + pan.x * scale + offsetXChange).coerceIn(minOffsetX, maxOffsetX),
                                (offset.y + pan.y * scale + offsetYChange).coerceIn(minOffsetY, maxOffsetX)
                            )
                        }
                    }
                } else Modifier
            )
            .then(
                if (isInteractive) Modifier.pointerInput(Unit) {
                    // Double Tap to zoom reset
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale != 1f) {
                                scale = 1f
                                offset = Offset(initialOffset.x, initialOffset.y)
                            } else {
                                scale = 2f
                            }
                        }
                    )
                } else Modifier
            )
            .then(
                if (isInteractive) Modifier.pointerInput(Unit) {
                    // Drag to highlight
                    detectDragGestures(
                        onDragStart = { dragOffset ->
                            val xStep = (canvasSize.width * scale) / (timeLabels.size.coerceAtLeast(1))
                            selectedIndex = ((dragOffset.x - offset.x) / xStep).toInt().coerceIn(0, timeLabels.size - 1)
                        },
                        onDrag = { change, _ ->
                            val xStep = (canvasSize.width * scale) / (timeLabels.size.coerceAtLeast(1))
                            selectedIndex = ((change.position.x - offset.x) / xStep).toInt().coerceIn(0, timeLabels.size - 1)
                        },
                        onDragEnd = {
                            selectedIndex = -1
                        }
                    )
                } else Modifier
            )
            .graphicsLayer {
                scaleX = if (isInteractive) scale else 1f
                scaleY = if (isInteractive) scale else 1f
                translationX = if (isInteractive) offset.x else 0f
                translationY = if (isInteractive) offset.y else 0f
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize(),
            onDraw = {
                canvasSize = size

                val maxValues = listOf(
                    waveHeight.maxOrNull() ?: 1f,
                    wavePeriod.maxOrNull() ?: 1f,
                    waveDirection.maxOrNull() ?: 1f
                )

                with(GraphPainter) {
                    drawGridLines()
                    drawYLabels(maxValues)
                    drawXLabels(timeLabels)
                    plotLines(waveHeight, wavePeriod, waveDirection, maxValues, selectedIndex)
                    drawCoordinate(selectedIndex, timeLabels.size)
                }
            }
        )
    }
    selectedIndex.takeIf { it != -1 }?.let {
        DrawCoordinateKey(selectedIndex, waveHeight, wavePeriod, waveDirection, timeLabels)
    }
}
/*
* Create Key for selected coordinates
* TODO: FIX the layout
 */
@Composable
private fun DrawCoordinateKey(
    selectedIndex: Int,
    waveHeight: List<Float>,
    wavePeriod: List<Float>,
    waveDirection: List<Float>,
    timeLabels: List<String>,
) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(6.dp)
            .shadow(1.dp)
    ) {
        Column( horizontalAlignment = Alignment.Start )
        {
            Text("Time: ${timeLabels[selectedIndex]}", fontSize = 12.sp)
            Text("Height: ${waveHeight.getOrNull(selectedIndex) ?: "-"} ft", color = Color.Blue, fontSize = 12.sp)
            Text("Period: ${wavePeriod.getOrNull(selectedIndex) ?: "-"} s", color = Color.Cyan, fontSize = 12.sp)
            Text(
                "Direction: ${waveDirection.getOrNull(selectedIndex) ?: "-"}°",
                color = Color.Green,
                fontSize = 12.sp
            )
        }
    }
}

@Preview
@Composable
fun PreviewDrawGraph(
){
    DrawServiceGraph(fakeWaveData)
}