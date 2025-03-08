package com.example.wavereader.data

import com.example.wavereader.model.HistoryRecord
import com.example.wavereader.model.WaveDataPoint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun fetchHistoryRecords(): List<HistoryRecord> {
        val user = auth.currentUser ?: return emptyList()

        return try {
            val snapshot = firestore
                .collection("waveHistory")
                .document(user.uid)
                .collection("sessions")
                .get()
                .await()

            snapshot.map { document ->
                val dataPoints = (document.get("dataPoints") as? List<Map<String, Any>>)?.map { point ->
                    WaveDataPoint(
                        time = (point["time"] as Number).toFloat(),
                        height = (point["height"] as Number).toFloat(),
                        period = (point["period"] as Number).toFloat(),
                        direction = (point["direction"] as Number).toFloat()
                    )
                } ?: emptyList()

                val timestampMillis = document.getLong("timestamp") ?: System.currentTimeMillis()
                val formattedTimestamp = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestampMillis))
                val lat = document.getDouble("lat")
                val lon = document.getDouble("lon")

                HistoryRecord(
                    id = document.id,
                    timestamp = formattedTimestamp,
                    location = document.getString("location") ?: "Unknown",
                    lat = lat,
                    lon = lon,
                    dataPoints = dataPoints
                )
            }
        } catch (e: Exception) {
            println("Error fetching history: $e")
            emptyList()
        }
    }
}