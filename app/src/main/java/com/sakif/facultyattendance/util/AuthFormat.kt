package com.sakif.facultyattendance.util

object AuthFormat {
    // Converts Faculty ID to email (adjust according to your university's domain)
    fun idToEmail(facultyId: String): String {
        return "$facultyId@university.com"
    }
}
