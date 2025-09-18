package com.sakif.facultyattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakif.facultyattendance.repository.AuthRepository
import com.sakif.facultyattendance.ui.SignInUiState
import com.sakif.facultyattendance.util.AuthFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState

    // Login function
    fun login(facultyId: String, password: String) {
        val id = facultyId.trim()
        if (id.isEmpty() || password.isEmpty()) {
            _uiState.value = SignInUiState.Error("Please enter both Faculty ID and password")
            return
        }
        _uiState.value = SignInUiState.Loading
        viewModelScope.launch {
            try {
                val success = authRepository.login(id, password)
                _uiState.value = if (success) {
                    SignInUiState.Success
                } else {
                    SignInUiState.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _uiState.value = SignInUiState.Error("Login failed: ${e.message}")
            }
        }
    }

    // Forgot password function
    fun forgotPassword(facultyId: String) {
        val id = facultyId.trim()
        if (id.isEmpty()) {
            _uiState.value = SignInUiState.Error("Enter your Faculty ID first")
            return
        }
        viewModelScope.launch {
            try {
                val email = AuthFormat.idToEmail(id)
                authRepository.sendPasswordReset(email)
                _uiState.value = SignInUiState.Success // Reset email sent
            } catch (e: Exception) {
                _uiState.value = SignInUiState.Error("Failed to send reset email: ${e.message}")
            }
        }
    }
}
