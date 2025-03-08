package com.example.wavereader.data

import com.example.wavereader.model.MeasuredWaveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecordSessionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun saveSession(
        measuredData: List<MeasuredWaveData>,
        locationName: String,
        latLng: Pair<Double, Double>?,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val userId = auth.currentUser?.uid ?: return
        if (measuredData.isEmpty()) return

        val sessionData = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "location" to locationName,
            "dataPoints" to measuredData.map { data ->
                mapOf(
                    "time" to data.time,
                    "height" to data.waveHeight,
                    "period" to data.wavePeriod,
                    "direction" to data.waveDirection
                )
            }
        )

        latLng?.let { (lat, lon) ->
            sessionData["lat"] = lat
            sessionData["lon"] = lon
        }

        firestore.collection("waveHistory")
            .document(userId)
            .collection("sessions")
            .add(sessionData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}