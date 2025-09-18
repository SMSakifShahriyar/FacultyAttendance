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

    fun login(facultyId: String, password: String) {
        val id = facultyId.trim()
        if (id.isEmpty() || password.isEmpty()) {
            _uiState.value = SignInUiState.Error("Please enter both Faculty ID and password")
            return
        }
        _uiState.value = SignInUiState.Loading
        viewModelScope.launch {
            try {
                // Attempt login with the given credentials
                val isAuthenticated = authRepository.login(id, password)
                _uiState.value = if (isAuthenticated) {
                    SignInUiState.Success
                } else {
                    SignInUiState.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _uiState.value = SignInUiState.Error("Login failed: ${e.message}")
            }
        }
    }

    // New function to handle forgot password
    fun forgotPassword(facultyId: String) {
        val id = facultyId.trim()
        if (id.isEmpty()) {
            _uiState.value = SignInUiState.Error("Enter your Faculty ID first")
            return
        }
        viewModelScope.launch {
            try {
                // Convert Faculty ID to email
                val email = AuthFormat.idToEmail(id)
                authRepository.sendPasswordReset(email)
                _uiState.value = SignInUiState.Success // Reset sent
            } catch (e: Exception) {
                _uiState.value = SignInUiState.Error("Failed to send reset email: ${e.message}")
            }
        }
    }
}
