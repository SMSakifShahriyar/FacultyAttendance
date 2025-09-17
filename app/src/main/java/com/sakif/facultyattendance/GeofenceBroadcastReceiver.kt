package com.sakif.facultyattendance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.Geofence

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent == null) {
                Log.e("GeofenceReceiver", "GeofencingEvent is null")
                return
            }

            if (geofencingEvent.hasError()) {
                val errorCode = geofencingEvent.errorCode
                Log.e("GeofenceReceiver", "Geofence error: $errorCode")
                return
            }

            val geofenceTransition = geofencingEvent.geofenceTransition

            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    Log.d("GeofenceReceiver", "Entered university geofence")
                    showToast(context, "You have entered the university area. Open app to check in.")
                }
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    Log.d("GeofenceReceiver", "Exited university geofence")
                    showToast(context, "You have left the university area. Remember to check out.")
                }
                else -> {
                    Log.w("GeofenceReceiver", "Unknown geofence transition: $geofenceTransition")
                }
            }
        } catch (e: Exception) {
            Log.e("GeofenceReceiver", "Error processing geofence event: ${e.message}")
        }
    }

    private fun showToast(context: Context, message: String) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("GeofenceReceiver", "Error showing toast: ${e.message}")
        }
    }
}
