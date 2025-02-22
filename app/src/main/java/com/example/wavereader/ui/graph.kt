package com.example.wavereader.ui

import android.graphics.Paint
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wavereader.model.Hourly
import com.example.wavereader.model.MeasuredWaveData
import com.example.wavereader.testData.fakeWaveData
import com.example.wavereader.ui.theme.customColor2Light
import com.example.wavereader.ui.theme.customColor4Light
import com.example.wavereader.ui.theme.customColor5Dark
import java.time.LocalDateTime


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

@Composable
fun DrawSensorGraph(waveData: List<MeasuredWaveData>) {
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
        timeLabels += if (index % step == 0 || index == totalPoints - 1) "${time.toInt()}s" else ""
    }

    Graph(waveHeight, wavePeriod, waveDirection, timeLabels)
}

// Formats API time data from ISO to hour.
fun formatTime(time: String): String {
    return LocalDateTime.parse(time).hour.toString()
}

@Composable
fun Graph(
    waveHeight: List<Float>,
    wavePeriod: List<Float>,
    waveDirection: List<Float>,
    timeLabels: List<String>
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
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .pointerInput(Unit) {
                // Zoom and Pan
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = scale * zoom
                    scale = newScale.coerceIn(minScale, maxScale)

                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val offsetXChange = (centerX - offset.x) * (newScale / scale - 1)
                    val offsetYChange = (centerY - offset.y) * (newScale / scale - 1)

                    // Calculate min and max offsets
                    val maxOffsetX = (size.width / 2) * (scale - 1)
                    val minOffsetX = -maxOffsetX
                    val maxOffsetY = (size.height / 2) * (scale - 1)
                    val minOffsetY = -maxOffsetY

                    // Update offsets while ensuring they stay within bounds
                    if (scale * zoom <= maxScale) {
                        offset = Offset(
                            (offset.x + pan.x * scale + offsetXChange).coerceIn(
                                minOffsetX,
                                maxOffsetX
                            ),
                            (offset.y + pan.y * scale + offsetYChange).coerceIn(
                                minOffsetY,
                                maxOffsetX
                            )
                        )
                    }

                    // Store initial offset on pan
                    if (pan != Offset(0f, 0f) && initialOffset == Offset(0f, 0f)) {
                        initialOffset = Offset(offset.x, offset.y)
                    }
                }
            }
            .pointerInput(Unit) {
                // Reset zoom and pan / quickly zoom
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
            }
            .pointerInput(Unit) {
                // Hold down and drag to highlight and view points
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
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize(),
            onDraw = {
                canvasSize = size // Store the canvas size dynamically

                val maxValues = listOf(
                    waveHeight.maxOrNull() ?: 1f,
                    wavePeriod.maxOrNull() ?: 1f,
                    waveDirection.maxOrNull() ?: 1f
                )

                drawGraphBorders()
                drawGridLines()
                drawYAxisLabels(maxValues)
                drawXAxisLabels(timeLabels)
                plotWaveLines(waveHeight, wavePeriod, waveDirection, maxValues, selectedIndex)
                drawSelectionIndicator(selectedIndex, timeLabels.size)
            }
        )

        selectedIndex.takeIf { it != -1 }?.let {
            DrawCoordinateKey(selectedIndex, waveHeight, wavePeriod, waveDirection, timeLabels)
        }
    }
}

/**
 * Draws the graph borders.
 */
private fun DrawScope.drawGraphBorders() {
    drawRect(Color.Black, style = Stroke(0.5.dp.toPx()))
}

/**
 * Draws horizontal grid lines.
 */
private fun DrawScope.drawGridLines() {
    val gridLines = 6
    val yStep = size.height / (gridLines + 1)
    val barWidthPx = 0.5.dp.toPx()

    repeat(gridLines) { i ->
        val y = yStep * (i + 1)
        drawLine(Color.Gray, Offset(0f, y), Offset(size.width, y), strokeWidth = barWidthPx)
    }
}

/**
 * Draws the Y-axis labels for height, period, and direction.
 */
private fun DrawScope.drawYAxisLabels(maxValues: List<Float>) {
    val labels = listOf("ft", "s", "°")
    val positions = listOf(size.width - 160f, size.width - 100f, size.width - 40f)
    val yStep = size.height / 7
    val labelPadding = 6.dp.toPx()

    for (i in 0..6) {
        val y = size.height - (i * yStep)
        maxValues.forEachIndexed { index, maxVal ->
            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.1f", (maxVal / 6 * i).toDouble()) + labels[index],
                positions[index],
                y - labelPadding,
                Paint().apply { textSize = 24f; color = android.graphics.Color.BLACK }
            )
        }
    }
}

/**
 * Draws X-axis labels.
 */
private fun DrawScope.drawXAxisLabels(timeLabels: List<String>) {
    val xStep = size.width / (timeLabels.size).coerceAtLeast(1)
    val labelPadding = 16.dp.toPx()

    timeLabels.forEachIndexed { index, label ->
        if (index % 2 == 0) {
            drawContext.canvas.nativeCanvas.drawText(
                label,
                xStep * index,
                size.height + labelPadding,
                Paint().apply {
                    textAlign = Paint.Align.CENTER
                    textSize = density.run { 12.sp.toPx() }
                }
            )
        }
    }
}

/**
 * Plots Wave Lines
 */
private fun DrawScope.plotWaveLines(
    waveHeight: List<Float>,
    wavePeriod: List<Float>,
    waveDirection: List<Float>,
    maxValues: List<Float>,
    selectedIndex: Int
) {
    val xStep = size.width / (waveHeight.size).coerceAtLeast(1)
    val graphHeight = size.height
    val colors = listOf(customColor5Dark, customColor4Light, customColor2Light)
    val dataSets = listOf(waveHeight, wavePeriod, waveDirection)

    // Draw Lines
    dataSets.forEachIndexed { index, data ->
        if (data.isNotEmpty()) {
            val normalizedData = data.map { (it / (maxValues[index] + 0.01f)) * graphHeight }
            for (i in 1 until normalizedData.size) {
                drawLine(
                    color = colors[index],
                    start = Offset((i - 1) * xStep, graphHeight - normalizedData[i - 1]),
                    end = Offset(i * xStep, graphHeight - normalizedData[i]),
                    strokeWidth = 4f
                )
            }

            // Draw selection point if within bounds
            if (selectedIndex in data.indices) {
                val selectedX = selectedIndex * xStep
                val selectedY = graphHeight - normalizedData[selectedIndex]

                drawCircle(
                    color = colors[index],
                    radius = 8f,
                    center = Offset(selectedX, selectedY)
                )
            }
        }
    }
}

/**
 * Draws the selection indicator when a point is tapped.
 */
private fun DrawScope.drawSelectionIndicator(selectedIndex: Int, totalLabels: Int) {
    if (selectedIndex != -1) {
        val selectedX = selectedIndex * (size.width / totalLabels)
        drawLine(Color.Red, Offset(selectedX, 0f), Offset(selectedX, size.height), strokeWidth = 2f)
    }
}

/**
 * Displays a coordinate key with selected data.
 */
@Composable
private fun DrawCoordinateKey(
    selectedIndex: Int,
    waveHeight: List<Float>,
    wavePeriod: List<Float>,
    waveDirection: List<Float>,
    timeLabels: List<String>
) {
    Column(
        modifier = Modifier
            //.align(Alignment.TopCenter)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text("Time: ${timeLabels[selectedIndex]}")
        Text("Height: ${waveHeight.getOrNull(selectedIndex) ?: "-"} ft", color = customColor5Dark)
        Text("Period: ${wavePeriod.getOrNull(selectedIndex) ?: "-"} s", color = customColor4Light)
        Text("Direction: ${waveDirection.getOrNull(selectedIndex) ?: "-"}°", color = customColor2Light)
    }
}

@Preview
@Composable
fun PreviewDrawGraph(
){
    //WavePeriodGraph(fakeWaveData)
    DrawServiceGraph(fakeWaveData)
}