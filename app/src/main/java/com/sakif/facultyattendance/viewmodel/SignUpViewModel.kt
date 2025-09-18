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

sealed class SignUpUiState {
    object Idle : SignUpUiState()
    object Loading : SignUpUiState()
    object Success : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun register(facultyId: String, password: String) {
        val id = facultyId.trim()
        if (id.isEmpty() || password.length < 6) {
            _uiState.value = SignUpUiState.Error("Enter a valid Faculty ID and a 6+ char password")
            return
        }
        if (!FacultyIds.allowed.contains(id)) {
            _uiState.value = SignUpUiState.Error("Faculty ID not authorized")
            return
        }

        _uiState.value = SignUpUiState.Loading
        viewModelScope.launch {
            try {
                val email = com.sakif.facultyattendance.util.AuthFormat.idToEmail(id)
                authRepository.signUp(email, password)

                val uid = authRepository.getCurrentUser()?.uid
                    ?: error("No user after sign up")

                val profile = mapOf(
                    "facultyId" to id,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("faculties").document(uid).set(profile)

                _uiState.value = SignUpUiState.Success
            } catch (e: Exception) {
                _uiState.value = SignUpUiState.Error(e.message ?: "Registration failed")
            }
        }
    }
}
