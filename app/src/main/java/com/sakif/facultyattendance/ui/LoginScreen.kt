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
import com.sakif.facultyattendance.viewmodel.SignInUiState
import com.sakif.facultyattendance.viewmodel.SignInViewModel

@Composable
fun LoginScreen(
    viewModel: SignInViewModel = hiltViewModel(),
    onSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    var facultyId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is SignInUiState.Success -> {
                Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            is SignInUiState.Error -> {
                Toast.makeText(context, (uiState as SignInUiState.Error).message, Toast.LENGTH_LONG).show()
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
        Text("Faculty Login", style = MaterialTheme.typography.headlineSmall)
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
            onClick = { viewModel.login(facultyId, password) },
            enabled = facultyId.isNotBlank() && password.isNotBlank()
        ) { Text(if (uiState is SignInUiState.Loading) "Logging in..." else "Login") }

        TextButton(onClick = { viewModel.forgotPassword(facultyId) }) {
            Text("Forgot password?")
        }
    }
}
