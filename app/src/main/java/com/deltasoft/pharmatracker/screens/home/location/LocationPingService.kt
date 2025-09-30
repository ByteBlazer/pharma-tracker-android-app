package com.deltasoft.pharmatracker.screens.home.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest

import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LocationPingService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Coroutine scope for network operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var locationHeartBeatFrequencyInSeconds : Int = 0
    private var token : String = ""

    private var sharedPrefsUtil : SharedPreferencesUtil? = null

    private var serviceStarted = false

    companion object {
        const val CHANNEL_ID = "APIPingServiceChannel"
        const val TAG = "APIPingService"
        const val ACTION_LOCATION_UPDATE = "com.deltasoft.pharmatracker.LOCATION_UPDATE"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (serviceStarted == false) {
            serviceStarted = true
            sharedPrefsUtil = SharedPreferencesUtil(this)
//        sharedPrefsUtil?.saveBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING,true)

            locationHeartBeatFrequencyInSeconds =
                sharedPrefsUtil?.getInt(PrefsKey.LOCATION_HEART_BEAT_FREQUENCY_IN_SECONDS) ?: 0
            token = AppUtils.createBearerToken(
                sharedPrefsUtil?.getString(PrefsKey.USER_ACCESS_TOKEN) ?: ""
            )

            Log.d(TAG, "delay in sec: " + getLocationHeartBeatInSeconds(applicationContext))

            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("API Pinging Service")
                .setContentText("Pinging API and getting location...")
                .setSmallIcon(com.deltasoft.pharmatracker.R.drawable.ic_share_location)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(1, notification)
            }

            // Use a Handler to trigger the location request and API ping every minute
            runnable = object : Runnable {
                override fun run() {
                    Log.d(TAG, "run: ")
                    token = AppUtils.createBearerToken(
                        sharedPrefsUtil?.getString(PrefsKey.USER_ACCESS_TOKEN) ?: ""
                    )
                    if (AppUtils.isValidToken(token)) {
                        Log.d(TAG, "run: valid token")
                        requestSingleLocationUpdate()
                    }else{
                        Log.d(TAG, "run: invalid token")
                    }
                    handler.postDelayed(this, (getLocationHeartBeatInSeconds(applicationContext) * 1000).toLong())
                }
            }
            handler.post(runnable)
        }else{
            Log.d(TAG, "onStartCommand: Service already started")
        }

        return START_STICKY
    }

    private fun getLocationHeartBeatInSeconds(applicationContext: Context?): Int {
        return  sharedPrefsUtil?.getInt(PrefsKey.LOCATION_HEART_BEAT_FREQUENCY_IN_SECONDS) ?: 0
    }

    private fun requestSingleLocationUpdate() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
//            .setExpirationDuration(1000) // This is the line that needs to be correct
            .setWaitForAccurateLocation(false)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permissions not granted.")
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                // We received a location, so we can now remove the updates
                fusedLocationClient.removeLocationUpdates(this)

                locationResult.lastLocation?.let {
                    // Ping your API here, now with a callback
                    pingAPIWithLocation(it) { success ->
                        if (success) {
                            // Send location via broadcast
                            val broadcastIntent = Intent(ACTION_LOCATION_UPDATE).apply {
                                putExtra(EXTRA_LATITUDE, it.latitude)
                                putExtra(EXTRA_LONGITUDE, it.longitude)
                            }
                            LocalBroadcastManager.getInstance(this@LocationPingService).sendBroadcast(broadcastIntent)
                            Log.d(TAG, "API Ping successful (from callback)")
                        } else {
                            Log.e(TAG, "API Ping failed (from callback)")
                        }
                        // You can add more logic here based on the success status
                    }
                }
            }
        }
    }

    // Modify the signature to include the callback
    private fun pingAPIWithLocation(location: Location, onResult: (success: Boolean) -> Unit) {
        val lat = location.latitude
        val lon = location.longitude

        serviceScope.launch {
            try {
                val locationData = LocationData(latitude = lat.toString(), longitude = lon.toString())
                val response = RetrofitClient.apiService.sendLocation(token, locationData)

                if (response.isSuccessful) {
                    Log.d(TAG, "Location sent successfully: Lat: $lat, Lon: $lon")
                    onResult(true) // Invoke callback with true for success
                } else {
                    Log.e(TAG, "Failed to send location. Code: ${response.code()}, Body: ${response.errorBody()?.string()}")
                    onResult(false) // Invoke callback with false for failure
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                onResult(false) // Invoke callback with false for network error
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error: ${e.message}")
                onResult(false) // Invoke callback with false for HTTP error
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        handler.removeCallbacks(runnable)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel() // Cancel all coroutines when the service is destroyed

//        sharedPrefsUtil?.saveBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING,false)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "API Ping Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}