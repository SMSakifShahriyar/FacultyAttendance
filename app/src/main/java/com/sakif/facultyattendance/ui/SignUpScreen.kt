package com.sakif.facultyattendance.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakif.facultyattendance.viewmodel.SignUpUiState
import com.sakif.facultyattendance.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var facultyId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (uiState) {
            is SignUpUiState.Success -> onSignUpSuccess()
            is SignUpUiState.Error -> {
                Toast.makeText(
                    context,
                    (uiState as SignUpUiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {}
        }
    }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = facultyId,
            onValueChange = { facultyId = it },
            label = { Text("Employee ID") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.register(facultyId.trim(), password) },
            enabled = facultyId.isNotBlank() && password.length >= 6
        ) {
            Text("Register")
        }
    }
}
