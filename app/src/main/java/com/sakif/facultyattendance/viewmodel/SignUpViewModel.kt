package com.sakif.facultyattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sakif.facultyattendance.util.FacultyIds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun register(facultyId: String, password: String) = viewModelScope.launch {
        if (!FacultyIds.allowed.contains(facultyId)) {
            _uiState.value = SignUpUiState.Error("Invalid faculty ID")
            return@launch
        }
        val email = "$facultyId@faculty.attendance.app"
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("No UID returned")
            firestore.collection("faculties")
                .document(uid)
                .set(mapOf("facultyId" to facultyId))
                .await()
            _uiState.value = SignUpUiState.Success
        } catch (e: Exception) {
            _uiState.value = SignUpUiState.Error(e.message ?: "Registration failed")
        }
    }
}
