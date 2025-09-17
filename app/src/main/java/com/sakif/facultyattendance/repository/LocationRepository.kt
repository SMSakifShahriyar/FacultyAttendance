package com.sakif.facultyattendance.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    fun isLocationWithinCampus(location: Location): Boolean {
        val campusLatitude = 23.883801837828987
        val campusLongitude = 90.36126734640744
        val campusRadius = 500.0 // meters

        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude, location.longitude,
            campusLatitude, campusLongitude,
            results
        )

        return results[0] <= campusRadius
    }
}
