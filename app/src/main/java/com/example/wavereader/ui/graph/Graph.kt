package com.example.wavereader.ui.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wavereader.model.MeasuredWaveData
import java.time.LocalDateTime

data class GraphLine(
    val values: List<Float>,
    val label: String,
    val color: Color,
    val unit: String
)

/*
* One Graph to rule them all
 */
@Composable
fun Graph(
    lines: List<GraphLine>,
    timeLabels: List<String>,
    isInteractive: Boolean = true,
    isScrollable: Boolean = false
) {
    var scrollOffset by remember { mutableFloatStateOf(0f) }
    val pointSpacing = 40f
    val graphWidth = (lines.firstOrNull()?.values?.size ?: 0) * pointSpacing
    var canvasWidth by remember { mutableFloatStateOf(0f) }

    var selectedIndex by remember { mutableIntStateOf(-1) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val dataSets = lines.map { it.values }
    val maxValues = lines.map { it.values.maxOrNull() ?: 1f }
    val colors = lines.map { it.color }
    val units = lines.map { it.unit }

    LaunchedEffect(lines.firstOrNull()?.values?.size) {
        if (isScrollable) {
            scrollOffset = (graphWidth - canvasWidth).coerceAtLeast(0f)
        }
    }

    Box(
        modifier = Modifier
        .height(300.dp)
        .clip(RectangleShape)
        .background(Color.White)
        .padding(horizontal = 8.dp, vertical = 12.dp)
        .then(
            if (isScrollable || isInteractive) Modifier.pointerInput(lines.firstOrNull()?.values?.size) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (isInteractive) {
                            val x = offset.x + if (isScrollable) scrollOffset else 0f
                            selectedIndex = (x / pointSpacing).toInt().coerceIn(0, timeLabels.size - 1)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (isScrollable) {
                            val maxScroll = (graphWidth - canvasSize.width).coerceAtLeast(0f)
                            scrollOffset = (scrollOffset - dragAmount.x).coerceIn(0f, maxScroll)
                        }

                        if (isInteractive) {
                            val x = change.position.x + if (isScrollable) scrollOffset else 0f
                            selectedIndex = (x / pointSpacing).toInt().coerceIn(0, timeLabels.size - 1)
                        }
                    },
                    onDragEnd = {
                        if (isInteractive) selectedIndex = -1
                    }
                )
            } else Modifier
        ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasWidth = it.width.toFloat() },
            onDraw = {
                canvasSize = size

                with(GraphPainter) {
                    drawGridLines()
                    drawYLabels(maxValues, units)
                    if (isScrollable) drawContext.canvas.save()
                    drawContext.canvas.translate(if (isScrollable) -scrollOffset else 0f, 0f)
                    drawXLabels(timeLabels)
                    plotLines(
                        dataSets = dataSets,
                        maxValues = maxValues,
                        colors = colors,
                        selectedIndex = selectedIndex
                    )
                    drawCoordinate(selectedIndex, timeLabels.size)
                }

                if (isScrollable) drawContext.canvas.restore()
            })
    }

    if (selectedIndex != -1) {
        DrawCoordinateKey(selectedIndex, lines, timeLabels)
    }
    // TODO: FIX LAYOUT
    GraphLegend(lines)
}

// Coordinate Input Selection
@Composable
fun DrawCoordinateKey(
    selectedIndex: Int,
    lines: List<GraphLine>,
    timeLabels: List<String>
) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(6.dp)
            .shadow(1.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text("Time: ${timeLabels.getOrNull(selectedIndex) ?: "-"}", fontSize = 12.sp)
            lines.forEach { line ->
                Text(
                    "${line.label}: ${line.values.getOrNull(selectedIndex) ?: "-"} ${line.unit}",
                    color = line.color,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Displays Line and Color Legend
@Composable
fun GraphLegend(lines: List<GraphLine>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        lines.forEach { line ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(line.color, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(4.dp))
                Text(line.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
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

