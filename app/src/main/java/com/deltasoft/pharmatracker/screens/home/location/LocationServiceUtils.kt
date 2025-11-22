package com.deltasoft.pharmatracker.screens.home.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationServiceUtils {
    fun startMyService(context: Context) {
        val serviceIntent = Intent(context, LocationPingService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopService(context: Context) {
        val serviceIntent = Intent(context, LocationPingService::class.java)
        context.stopService(serviceIntent)
    }


    fun restartForegroundService(context: Context) {
        val serviceIntent = Intent(context, LocationPingService::class.java)

        // A. Stop the service first (This will call onDestroy() in your Service)
        context.stopService(serviceIntent)

        // B. Start the service again immediately (This will call onCreate() and then onStartCommand())
        // Use startForegroundService for a foreground service, especially on newer Android versions (API 26+)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun getFusedLocationClient(context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onSuccess: (Location) -> Unit, // Callback for successful result
        onFailure: (Exception) -> Unit  // Callback for failure
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        // Request settings for high accuracy
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationClient.getCurrentLocation(request, cancellationTokenSource.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onSuccess(location)
                } else {
                    onFailure(Exception("Location data is null (GPS may be disabled)."))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun isLocationServiceRunning(): Boolean {
        return LocationPingService.isServiceRunning
    }

    fun isLocationServiceNotRunning(): Boolean {
        return !LocationPingService.isServiceRunning
    }
}