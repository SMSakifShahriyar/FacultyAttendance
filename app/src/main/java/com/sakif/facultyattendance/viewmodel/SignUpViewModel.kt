package com.sakif.facultyattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.sakif.facultyattendance.repository.AuthRepository
import com.sakif.facultyattendance.util.FacultyIds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun register(facultyId: String, password: String) {
        if (facultyId.isBlank() || password.length < 6) {
            _uiState.value = SignUpUiState.Error("Enter a valid Faculty ID and a 6+ char password")
            return
        }
        if (!FacultyIds.allowed.contains(facultyId)) {
            _uiState.value = SignUpUiState.Error("Faculty ID not authorized")
            return
        }

        _uiState.value = SignUpUiState.Loading
        viewModelScope.launch {
            try {
                // You can choose any deterministic email scheme; here we derive email from facultyId
                val email = "$facultyId@faculty.attendance.app"
                authRepository.signUp(email, password)

                val uid = authRepository.getCurrentUser()?.uid
                    ?: throw IllegalStateException("No user after sign up")

                // Save a minimal faculty profile
                val profile = mapOf(
                    "facultyId" to facultyId,
                    "createdAt" to System.currentTimeMillis()
                )

                // Using set() to create/update the doc for this user
                firestore.collection("faculties")
                    .document(uid)
                    .set(profile)
                    .addOnFailureListener { /* no-op: handled by try/catch if awaited */ }

                _uiState.value = SignUpUiState.Success
            } catch (e: Exception) {
                _uiState.value = SignUpUiState.Error(e.message ?: "Registration failed")
            }
        }
    }
}
