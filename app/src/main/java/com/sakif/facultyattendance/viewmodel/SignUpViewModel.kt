package com.sakif.facultyattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakif.facultyattendance.repository.AuthRepository
import com.sakif.facultyattendance.ui.SignUpUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun signUp(facultyId: String, password: String) {
        val id = facultyId.trim()
        if (id.isEmpty() || password.isEmpty()) {
            _uiState.value = SignUpUiState.Error("Please fill in both fields")
            return
        }
        _uiState.value = SignUpUiState.Loading
        viewModelScope.launch {
            try {
                // Sign up logic
                val isSignedUp = authRepository.signUp(id, password)
                _uiState.value = if (isSignedUp) {
                    SignUpUiState.Success
                } else {
                    SignUpUiState.Error("Failed to sign up")
                }
            } catch (e: Exception) {
                _uiState.value = SignUpUiState.Error("Sign up failed: ${e.message}")
            }
        }
    }
}
