package com.sakif.facultyattendance.viewmodel

sealed class SignUpUiState {
    object Idle : SignUpUiState()
    object Success : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}
