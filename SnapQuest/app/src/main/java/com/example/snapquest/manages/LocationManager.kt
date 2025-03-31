package com.example.snapquest.manages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

object LocationManager {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
    }

    suspend fun getLastLocation(): Location? {
        return if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val cancellationToken = CancellationTokenSource()
                val locationResult = fusedLocationClient.getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                ).await()

                locationResult
            } catch (e: Exception) {
                fusedLocationClient.lastLocation.await()
            }
        } else {
            null
        }
    }
}