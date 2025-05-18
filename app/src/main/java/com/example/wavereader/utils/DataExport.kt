package com.example.wavereader.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.wavereader.model.HistoryRecord
import org.json.JSONArray
import org.json.JSONObject

/**
 * Write to file in CVS format
 * */
fun exportToCsv(context: Context, uri: Uri, data: List<HistoryRecord>) {
    try {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            val writer = output.bufferedWriter()
            writer.write("Record ID,Timestamp,Location,Latitude,Longitude,Height,Period,Direction\n")
            data.forEach { record ->
                val lat = record.lat?.toString() ?: ""
                val lon = record.lon?.toString() ?: ""
                val timestamp = record.timestamp.replace("," , "")
                val location = record.location.replace("," , "")
                record.dataPoints.forEach { point ->
                    writer.write("${record.id},$timestamp,$location,$lat,$lon,${point.height},${point.period},${point.direction}\n")
                }
            }
            writer.flush()
        }
        Toast.makeText(context, "CSV exported successfully!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "CSV export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

/**
 *  Write to file in JSON format
 *  */
fun exportToJson(context: Context, uri: Uri, data: List<HistoryRecord>) {
    try {
        val jsonArray = JSONArray()
        data.forEach { record ->
            record.dataPoints.forEach { point ->
                val json = JSONObject().apply {
                    put("recordId", record.id)
                    put("timestamp", record.timestamp)
                    put("location", record.location)
                    put("height", point.height)
                    put("period", point.period)
                    put("direction", point.direction)
                    record.lat?.let { put("lat", it) }
                    record.lon?.let { put("lon", it) }
                }
                jsonArray.put(json)
            }
        }

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.bufferedWriter().use { out ->
                out.write(jsonArray.toString(2))
            }
        }

        Toast.makeText(context, "JSON exported successfully!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "JSON export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

