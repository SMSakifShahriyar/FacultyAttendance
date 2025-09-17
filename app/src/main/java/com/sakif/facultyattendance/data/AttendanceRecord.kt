package com.sakif.facultyattendance.data

import com.google.firebase.Timestamp

data class AttendanceRecord(
    val id: String = "",
    val facultyId: String = "",
    val eventType: String = "",   // "check-in" or "check-out"
    val date: String = "",
    val timestamp: Timestamp? = null
)
