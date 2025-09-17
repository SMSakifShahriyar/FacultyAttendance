package com.sakif.facultyattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakif.facultyattendance.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling faculty login.
 *
 * The [facultyId] entered by the user is validated against a synthetic email
 * domain before calling Firebase Auth.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Attempts to sign in a faculty member given their [facultyId] and [password].
     * Constructs an email of the form "<facultyId>@faculty.attendance.app".
     */
    fun login(facultyId: String, password: String) {
        if (facultyId.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Faculty ID and password cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = LoginUiState.Loading

                // Build the synthetic email from the faculty ID
                val email = "$facultyId@faculty.attendance.app"

                // Delegate to AuthRepository
                authRepository.signIn(email, password)

                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(
                    e.message ?: "Login failed. Please try again."
                )
            }
        }
    }
}

/**
 * UI state for the login screen.
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
