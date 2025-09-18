package com.sakif.facultyattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakif.facultyattendance.repository.AuthRepository
import com.sakif.facultyattendance.util.AuthFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SignInUiState {
    object Idle : SignInUiState()
    object Loading : SignInUiState()
    object Success : SignInUiState()
    data class Error(val message: String) : SignInUiState()
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val uiState: StateFlow<SignInUiState> = _uiState

    fun login(facultyId: String, password: String) {
        val id = facultyId.trim()
        if (id.isEmpty() || password.isEmpty()) {
            _uiState.value = SignInUiState.Error("Enter Faculty ID and password")
            return
        }
        _uiState.value = SignInUiState.Loading
        viewModelScope.launch {
            try {
                val email = AuthFormat.idToEmail(id)
                authRepository.signIn(email, password)
                _uiState.value = SignInUiState.Success
            } catch (e: Exception) {
                _uiState.value = SignInUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun forgotPassword(facultyId: String) {
        val id = facultyId.trim()
        if (id.isEmpty()) {
            _uiState.value = SignInUiState.Error("Enter Faculty ID to reset password")
            return
        }
        viewModelScope.launch {
            try {
                authRepository.sendPasswordReset(AuthFormat.idToEmail(id))
                _uiState.value = SignInUiState.Idle
            } catch (e: Exception) {
                _uiState.value = SignInUiState.Error(e.message ?: "Could not send reset email")
            }
        }
    }
}
