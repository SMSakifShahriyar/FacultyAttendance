package com.sakif.facultyattendance.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakif.facultyattendance.repository.AttendanceRepository
import com.sakif.facultyattendance.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AttendanceUiState>(AttendanceUiState.Idle)
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    fun markAttendance(eventType: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AttendanceUiState.Loading

                // Check if inside campus
                val location = locationRepository.getCurrentLocation()
                if (!isInsideCampus(location)) {
                    _uiState.value = AttendanceUiState.Error(
                        "You must be inside the university premises to mark attendance."
                    )
                    return@launch
                }

                // Check existing attendance for today
                if (eventType == "check-in" && attendanceRepository.hasCheckedInToday()) {
                    _uiState.value = AttendanceUiState.Error("Already checked in today.")
                    return@launch
                }

                // Mark attendance
                attendanceRepository.markAttendance(eventType)
                _uiState.value = AttendanceUiState.Success("$eventType recorded successfully!")

            } catch (e: Exception) {
                _uiState.value = AttendanceUiState.Error(
                    e.message ?: "Failed to mark attendance"
                )
            }
        }
    }

    fun checkTodayAttendance() {
        viewModelScope.launch {
            try {
                val hasCheckedIn = attendanceRepository.hasCheckedInToday()
                if (hasCheckedIn) {
                    _uiState.value = AttendanceUiState.Success("Already checked in today")
                } else {
                    _uiState.value = AttendanceUiState.Idle
                }
            } catch (e: Exception) {
                _uiState.value = AttendanceUiState.Error("Failed to check attendance status")
            }
        }
    }

    private fun isInsideCampus(location: Location?): Boolean {
        if (location == null) return false

        val campusLocation = Location("campus").apply {
            latitude = 23.883801837828987
            longitude = 90.36126734640744
        }

        val distance = location.distanceTo(campusLocation)
        return distance <= 500f
    }
}

sealed class AttendanceUiState {
    object Idle : AttendanceUiState()
    object Loading : AttendanceUiState()
    data class Success(val message: String) : AttendanceUiState()
    data class Error(val message: String) : AttendanceUiState()
}
