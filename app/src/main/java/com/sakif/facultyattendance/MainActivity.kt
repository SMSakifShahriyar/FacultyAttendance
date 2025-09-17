package com.sakif.facultyattendance

import android.annotation.SuppressLint
import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.sakif.facultyattendance.ui.theme.FacultyAttendanceTheme
import com.sakif.facultyattendance.viewmodel.AttendanceViewModel
import com.sakif.facultyattendance.viewmodel.AttendanceUiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(this)
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {
            setupGeofence()
        } else {
            Toast.makeText(this, "Location permissions are required for attendance tracking.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            FacultyAttendanceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AttendanceScreen()
                }
            }
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            setupGeofence()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupGeofence() {
        try {
            val campusLatitude = 23.883801837828987
            val campusLongitude = 90.36126734640744
            val geofenceRadius = 500f

            val geofence = Geofence.Builder()
                .setRequestId("university_geofence")
                .setCircularRegion(campusLatitude, campusLongitude, geofenceRadius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener {
                    Log.d("Geofence", "Geofence added successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Geofence", "Failed to add geofence: ${e.message}")
                    Toast.makeText(this, "Failed to setup geofencing: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("Geofence", "Error setting up geofence: ${e.message}")
            Toast.makeText(this, "Error setting up location tracking", Toast.LENGTH_SHORT).show()
        }
    }

    @Composable
    fun AttendanceScreen(
        viewModel: AttendanceViewModel = hiltViewModel()
    ) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val user = FirebaseAuth.getInstance().currentUser

        LaunchedEffect(Unit) {
            viewModel.checkTodayAttendance()
        }

        // Use local variable for smart cast
        val currentUiState = uiState

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
                        text = "Welcome, ${user?.email ?: "Faculty"}",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (currentUiState) {
                        is AttendanceUiState.Loading -> {
                            CircularProgressIndicator()
                            Text(
                                text = "Processing...",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        is AttendanceUiState.Success -> {
                            Text(
                                text = currentUiState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        is AttendanceUiState.Error -> {
                            Text(
                                text = currentUiState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        else -> {
                            Text(
                                text = "Ready to mark attendance",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.markAttendance("check-in") },
                            enabled = currentUiState !is AttendanceUiState.Loading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Check In")
                        }

                        Button(
                            onClick = { viewModel.markAttendance("check-out") },
                            enabled = currentUiState !is AttendanceUiState.Loading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Check Out")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }
            ) {
                Text("Sign Out")
            }
        }
    }
}
