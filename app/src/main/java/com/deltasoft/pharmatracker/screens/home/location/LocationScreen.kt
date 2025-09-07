package com.deltasoft.pharmatracker.screens.home.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale


private const val TAG = "LocationScreen"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationScreen(
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
//
//    val locationPermissionsState = rememberMultiplePermissionsState(
//        permissions = listOf(
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        )
//    )

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val latitude by locationViewModel.latitude.collectAsState()
    val longitude by locationViewModel.longitude.collectAsState()

    DisposableEffect(locationViewModel) {
        locationViewModel.registerReceiver(context)
        onDispose {
            locationViewModel.unregisterReceiver(context)
        }
    }

    // Check the service status when the composable enters the screen
    LaunchedEffect(Unit) {
        locationViewModel.initialize(context)
    }

    val isRunning by locationViewModel.isServiceRunning.collectAsState()


    // Check the permission status
    val allPermissionsGranted = locationPermissionState.status.isGranted
    val shouldShowRationale = locationPermissionState.status.shouldShowRationale

    val message = if (allPermissionsGranted) {
        if (isRunning) {
            "Please wait for location update."
        } else {
            "Location not available. Press Start to get location."
        }
    } else if (shouldShowRationale)
        "The app needs this permission to function. Please grant it."
    else {
        "Permission is permanently denied. Go to settings to enable it."
    }

    // This LaunchedEffect will handle the initial request
    LaunchedEffect(locationPermissionState) {
        if (!locationPermissionState.status.isGranted && !locationPermissionState.status.shouldShowRationale) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (longitude != null && latitude != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            text = "Latitude: ${latitude}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Longitude: ${longitude}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = message,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            when {
                allPermissionsGranted -> {
//                    Text("Permission granted! You can use the feature.")
                    Button(
                        onClick = {
                            startMyService(context)
                        },
                        enabled = !isRunning
                    ) {
                        Text("Start Location Ping")
                    }
                }

                shouldShowRationale -> {
                    // Show rationale for the second or subsequent denial
//                    Text("The app needs this permission to function. Please grant it.")
                    Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                        Text("Request Permission")
                    }
                }

                else -> {
                    // Permission permanently denied: ask user to go to settings
//                    Text("Permission is permanently denied. Go to settings to enable it.")
                    Button(onClick = { openAppSettings(context) }) {
                        Text("Open App Settings")
                    }
                }
            }


            Button(
                onClick = {
                    stopService(context)
                    locationViewModel.clearLocationValues()
                },
                enabled = isRunning
            ) {
                Text("Stop Location Ping")
            }
        }
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}

private fun startMyService(context: Context) {
    val serviceIntent = Intent(context, LocationPingService::class.java)
    context.startForegroundService(serviceIntent)
}

private fun stopService(context: Context) {
    val serviceIntent = Intent(context, LocationPingService::class.java)
    context.stopService(serviceIntent)
}
