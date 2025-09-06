package com.deltasoft.pharmatracker.screens.home.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState


private const val TAG = "LocationScreen"
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationScreen(
    latitude: Double?,
    longitude: Double?,
    locationViewModel: LocationViewModel
) {
    val context = LocalContext.current

    val sharedPrefsUtil = SharedPreferencesUtil(context)

    val isServiceRunning = remember {
        sharedPrefsUtil.getBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING)
    }

    Log.d(TAG, "SREEEEENATH: isServiceRunning "+isServiceRunning)

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )


    val isRunning by locationViewModel.isServiceRunning.collectAsState()

    // Check the service status when the composable enters the screen
    LaunchedEffect(Unit) {
        locationViewModel.checkServiceStatus(context)
    }


    Log.d(TAG, "LocationScreen: isRunning "+isRunning)


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (longitude != null && latitude !=null) {
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
                    val message = if (isRunning){
                        "Please wait for location update."
                    }else{
                        "Location not available. Press Start to get location."
                    }
                    Text(
                        text =  message,
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
            Button(
                onClick = {
                    if (locationPermissionsState.allPermissionsGranted) {
                        // Permissions are granted, start getting location
                        startMyService(context)

                        locationViewModel.checkServiceStatus(context)
                    } else {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                },
                enabled = !isRunning
            ) {
                Text("Start Location Ping")
            }

            Button(
                onClick = {
                    stopService(context)
                    locationViewModel.stopService()
                    locationViewModel.checkServiceStatus(context)
                },
                enabled = isRunning
            ) {
                Text("Stop Location Ping")
            }
        }
    }
}

private fun startMyService(context: Context) {
    val serviceIntent = Intent(context, APIPingService::class.java)
    context.startForegroundService(serviceIntent)
}

private fun stopService(context: Context) {
    val serviceIntent = Intent(context, APIPingService::class.java)
    context.stopService(serviceIntent)
}
