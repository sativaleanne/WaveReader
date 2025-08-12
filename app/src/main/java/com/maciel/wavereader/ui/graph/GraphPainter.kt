package com.maciel.wavereader.ui.graph

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

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

    fun DrawScope.drawYLabels(maxValues: List<Float>, units: List<String>) {
        val positions = units.indices.map { i -> size.width - 160f + i * 60f }
        val yStep = size.height / 6
        val labelPadding = 2.dp.toPx()

        for (i in 0..6) {
            val y = size.height - (yStep * i)
            maxValues.forEachIndexed { index, maxVal ->
                val labelValue = (maxVal / 6f * i)
                drawContext.canvas.nativeCanvas.drawText(
                    String.format(Locale.US, "%.1f", labelValue) + units[index],
                    positions[index],
                    y - labelPadding,
                    Paint().apply { textSize = 24f; color = android.graphics.Color.BLACK }
                )
            }
        }
    }

    fun DrawScope.drawForecastLine(index: Int, pointSpacing: Float) {
        val x = index * pointSpacing
        drawLine(
            color = Color.Red,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 4f
        )
    }

    fun DrawScope.plotLines(
        dataSets: List<List<Float>>,
        maxValues: List<Float>,
        colors: List<Color>,
        selectedIndex: Int,
        pointSpacing: Float,
        graphHeight: Float
    ) {
        dataSets.forEachIndexed { index, data ->
            if (data.isNotEmpty()) {
                val normalized = data.map { (it / (maxValues[index] + 0.01f)) * graphHeight }

                for (i in 1 until normalized.size) {
                    drawLine(
                        color = colors[index],
                        start = Offset((i - 1) * pointSpacing, graphHeight - normalized[i - 1]),
                        end = Offset(i * pointSpacing, graphHeight - normalized[i]),
                        strokeWidth = 4f
                    )
                }

                if (selectedIndex in data.indices) {
                    drawCircle(
                        color = colors[index],
                        radius = 8f,
                        center = Offset(
                            selectedIndex * pointSpacing,
                            graphHeight - normalized[selectedIndex]
                        )
                    )
                }
            }
        }
    }

    fun DrawScope.drawXLabels(timeLabels: List<String>, pointSpacing: Float) {
        val labelPadding = 12.dp.toPx()
        timeLabels.forEachIndexed { index, label ->
            if (index % 2 == 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    index * pointSpacing,
                    size.height + labelPadding,
                    Paint().apply {
                        textAlign = Paint.Align.CENTER
                        textSize = density.run { 12.sp.toPx() }
                    }
                )
            }
        }
    }

    fun DrawScope.drawCoordinate(selectedIndex: Int, totalPoints: Int, pointSpacing: Float) {
        if (selectedIndex != -1) {
            val x = selectedIndex * pointSpacing
            drawLine(Color.Red, Offset(x, 0f), Offset(x, size.height), strokeWidth = 2f)
        }
    }
}

