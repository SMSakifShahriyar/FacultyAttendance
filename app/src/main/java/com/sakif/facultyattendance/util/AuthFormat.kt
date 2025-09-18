package com.sakif.facultyattendance.util

object AuthFormat {
    fun idToEmail(facultyId: String): String {
        // Simple conversion, change as necessary based on your logic
        return "$facultyId@university.com"
    }
}
