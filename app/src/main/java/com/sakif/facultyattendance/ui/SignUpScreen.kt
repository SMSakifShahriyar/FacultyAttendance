package com.sakif.facultyattendance.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakif.facultyattendance.viewmodel.SignUpUiState
import com.sakif.facultyattendance.viewmodel.SignUpViewModel

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var facultyId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is SignUpUiState.Success -> {
                Toast.makeText(context, "Registered successfully", Toast.LENGTH_SHORT).show()
            }
            is SignUpUiState.Error -> {
                Toast.makeText(context, (uiState as SignUpUiState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Faculty Registration", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = facultyId,
            onValueChange = { facultyId = it },
            label = { Text("Faculty ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.register(facultyId.trim(), password) },
            enabled = facultyId.isNotBlank() && password.length >= 6
        ) {
            Text(if (uiState is SignUpUiState.Loading) "Registering..." else "Register")
        }
    }
}
