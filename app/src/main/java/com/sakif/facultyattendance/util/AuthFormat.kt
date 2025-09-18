package com.sakif.facultyattendance.util

object AuthFormat {
    fun idToEmail(id: String): String = "${id.trim()}@faculty.attendance.app"
}
