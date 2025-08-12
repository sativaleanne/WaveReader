package com.maciel.wavereader.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maciel.wavereader.model.HistoryRecord
import com.maciel.wavereader.model.WaveDataPoint
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun fetchHistoryRecords(
        locationQuery: String = "",
        sortDescending: Boolean = true,
        startDateMillis: Long? = null,
        endDateMillis: Long? = null
    ): List<HistoryRecord> {
        val user = auth.currentUser ?: return emptyList()

        return try {
            var query = firestore
                .collection("waveHistory")
                .document(user.uid)
                .collection("sessions")
                .orderBy("timestamp", if (sortDescending) Query.Direction.DESCENDING else Query.Direction.ASCENDING)

            if (startDateMillis != null) {
                query = query.whereGreaterThanOrEqualTo("timestamp", startDateMillis)
            }
            if (endDateMillis != null) {
                query = query.whereLessThanOrEqualTo("timestamp", endDateMillis)
            }

            val snapshot = query.get().await()

            snapshot.mapNotNull { document ->
                val location = document.getString("location") ?: return@mapNotNull null
                if (locationQuery.isNotBlank() && !location.contains(locationQuery, ignoreCase = true)) {
                    return@mapNotNull null
                }

                val dataPoints = (document.get("dataPoints") as? List<Map<String, Any>>)?.map { point ->
                    WaveDataPoint(
                        time = (point["time"] as Number).toFloat(),
                        height = (point["height"] as Number).toFloat(),
                        period = (point["period"] as Number).toFloat(),
                        direction = (point["direction"] as Number).toFloat()
                    )
                } ?: emptyList()

                val timestampMillis = document.getLong("timestamp") ?: return@mapNotNull null
                val formattedTimestamp = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestampMillis))
                val lat = document.getDouble("lat")
                val lon = document.getDouble("lon")

                HistoryRecord(
                    id = document.id,
                    timestamp = formattedTimestamp,
                    location = location,
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