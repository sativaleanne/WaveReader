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
