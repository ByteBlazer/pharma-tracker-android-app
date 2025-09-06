package com.deltasoft.pharmatracker.screens.home.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
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

class APIPingService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null

    // Coroutine scope for network operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var locationHeartBeatFrequencyInSeconds : Int = 0
    private var token : String = ""

    private var sharedPrefsUtil : SharedPreferencesUtil? = null

    companion object {
        const val CHANNEL_ID = "APIPingServiceChannel"
        const val TAG = "APIPingService"
//        const val PING_INTERVAL_MS = 10 * 1000L // 1 minute
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
        sharedPrefsUtil = SharedPreferencesUtil(this)
        sharedPrefsUtil?.saveBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING,true)

        Log.d(TAG, "SREEEEENATH: "+sharedPrefsUtil?.getBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING))
        locationHeartBeatFrequencyInSeconds = sharedPrefsUtil?.getInt(PrefsKey.LOCATION_HEART_BEAT_FREQUENCY_IN_SECONDS)?:0
        token = AppUtils.createBearerToken(sharedPrefsUtil?.getString(PrefsKey.USER_ACCESS_TOKEN)?:"")
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("API Pinging Service")
            .setContentText("Pinging API and getting location...")
//            .setSmallIcon()
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, notification)
        }

        // Use a Handler to trigger the location request and API ping every minute
        runnable = object : Runnable {
            override fun run() {
                requestSingleLocationUpdate()
                handler.postDelayed(this, (locationHeartBeatFrequencyInSeconds*1000).toLong())
            }
        }
        handler.post(runnable)

        return START_STICKY
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
                    // Send location via broadcast
                    val broadcastIntent = Intent(ACTION_LOCATION_UPDATE).apply {
                        putExtra(EXTRA_LATITUDE, it.latitude)
                        putExtra(EXTRA_LONGITUDE, it.longitude)
                    }
                    LocalBroadcastManager.getInstance(this@APIPingService).sendBroadcast(broadcastIntent)

                    Log.d(TAG, "onReceive: latitude "+it.latitude)
                    Log.d(TAG, "onReceive: longitude "+it.longitude)

                    // Ping your API here
                    pingAPIWithLocation(it)
                }
            }
        }
    }


    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,
            (locationHeartBeatFrequencyInSeconds*1000).toLong()
        )
            .setWaitForAccurateLocation(false)
            .build()

        // Check for location permissions at runtime
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are not granted. This should be handled in the Activity before starting the service.
            Log.e(TAG, "Location permissions not granted.")
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun pingAPIWithLocation(location: Location) {
        val lat = location.latitude
        val lon = location.longitude

        Toast.makeText(applicationContext,"latitude $lat longitude $lon",Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Pinging API with Lat: $lat, Lon: $lon")
        // Your API call logic here, e.g., using a network library

        serviceScope.launch {
            try {
                val locationData = LocationData(latitude = lat.toString(), longitude = lon.toString())
                val response = RetrofitClient.apiService.sendLocation(token,locationData)

                if (response.isSuccessful) {
                    Log.d(TAG, "Location sent successfully: Lat: $lat, Lon: $lon")
                } else {
                    Log.e(TAG, "Failed to send location. Code: ${response.code()}, Body: ${response.errorBody()?.string()}")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error: ${e.message}")
            }
        }
    }

    private fun pingAPI() {
        // This is where you'd place your API call logic.
        Log.d(TAG, "API is being pinged at: ${System.currentTimeMillis()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel() // Cancel all coroutines when the service is destroyed

        sharedPrefsUtil?.saveBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING,false)
        Log.d(TAG, "Service is being destroyed.")
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