package com.sakif.facultyattendance.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sakif.facultyattendance.data.AttendanceRecord
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun markAttendance(eventType: String) {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val timestamp = System.currentTimeMillis()

        val attendanceData = hashMapOf(
            "faculty_id" to userId,
            "timestamp" to FieldValue.serverTimestamp(),
            "event_type" to eventType,
            "date" to today,
            "local_timestamp" to timestamp
        )

        firestore.collection("attendance")
            .document("${userId}_${today}_${eventType}")
            .set(attendanceData)
            .await()
    }

    suspend fun hasCheckedInToday(): Boolean {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val document = firestore.collection("attendance")
            .document("${userId}_${today}_check-in")
            .get()
            .await()

        return document.exists()
    }

    suspend fun getTodayAttendance(): List<AttendanceRecord> {
        val userId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val querySnapshot = firestore.collection("attendance")
            .whereEqualTo("faculty_id", userId)
            .whereEqualTo("date", today)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                AttendanceRecord(
                    id = document.id,
                    facultyId = document.getString("faculty_id") ?: "",
                    eventType = document.getString("event_type") ?: "",
                    date = document.getString("date") ?: "",
                    timestamp = document.getTimestamp("timestamp")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
