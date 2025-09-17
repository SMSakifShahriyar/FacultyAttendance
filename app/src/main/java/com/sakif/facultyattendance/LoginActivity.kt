package com.sakif.facultyattendance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakif.facultyattendance.ui.theme.FacultyAttendanceTheme
import com.sakif.facultyattendance.viewmodel.LoginViewModel
import com.sakif.facultyattendance.viewmodel.LoginUiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FacultyAttendanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onLoginSuccess = {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun LoginScreen(
        onLoginSuccess: () -> Unit,
        viewModel: LoginViewModel = hiltViewModel()
    ) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        LaunchedEffect(uiState) {
            val currentUiState = uiState

            when (currentUiState) {
                is LoginUiState.Error -> {
                    Toast.makeText(this@LoginActivity, currentUiState.message, Toast.LENGTH_SHORT).show()
                }
                else -> { /* No action needed */ }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Faculty Login",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is LoginUiState.Loading,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = uiState !is LoginUiState.Loading,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is LoginUiState.Loading && email.isNotBlank() && password.isNotBlank()
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Login")
                        }
                    }
                }
            }
        }
    }
}
