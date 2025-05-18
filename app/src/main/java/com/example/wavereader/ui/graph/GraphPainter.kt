package com.example.wavereader.ui.graph

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object GraphPainter {

    fun DrawScope.drawGridLines() {
        val gridLines = 8
        val yStep = size.height / gridLines
        val barWidthPx = 0.5.dp.toPx()

        drawLine(Color.Gray, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = barWidthPx)
        repeat(gridLines) { i ->
            val y = yStep * (i + 1)
            drawLine(Color.Gray, Offset(0f, y), Offset(size.width, y), strokeWidth = barWidthPx)
        }
    }

    // TODO: Adjust Labels to hug edge of graph
    fun DrawScope.drawYLabels(maxValues: List<Float>, units: List<String>) {
        val positions = units.indices.map { i -> size.width - 160f + i * 60f }
        val yStep = size.height / 8
        val labelPadding = 2.dp.toPx()

        for (i in 0..7) {
            val y = size.height - (yStep * i)
            maxValues.forEachIndexed { index, maxVal ->
                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%.1f", (maxVal / 6 * i).toDouble()) + units[index],
                    positions[index],
                    y - labelPadding,
                    Paint().apply { textSize = 24f; color = android.graphics.Color.BLACK }
                )
            }
        }
    }

    fun DrawScope.drawXLabels(timeLabels: List<String>) {
        val xStep = size.width / (timeLabels.size).coerceAtLeast(1)
        val labelPadding = 12.dp.toPx()

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

    fun DrawScope.plotLines(
        dataSets: List<List<Float>>,
        maxValues: List<Float>,
        colors: List<Color>,
        selectedIndex: Int
    ) {
        val pointCount = dataSets.firstOrNull()?.size ?: 1
        val xStep = size.width / pointCount.toFloat()
        val graphHeight = size.height

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

                if (selectedIndex in data.indices) {
                    drawCircle(
                        color = colors[index],
                        radius = 8f,
                        center = Offset(selectedIndex * xStep, graphHeight - normalizedData[selectedIndex])
                    )
                }
            }
        }
    }

    fun DrawScope.plotForecastLines() {
        // TODO
    }

    fun DrawScope.drawCoordinate(selectedIndex: Int, totalLabels: Int) {
        if (selectedIndex != -1) {
            val selectedX = selectedIndex * (size.width / totalLabels)
            drawLine(Color.Red, Offset(selectedX, 0f), Offset(selectedX, size.height), strokeWidth = 2f)
        }
    }
}
