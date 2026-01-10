package com.deltasoft.pharmatracker.screens.home.location


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.deltasoft.pharmatracker.screens.home.location.LocationPingService.Companion
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LocationService : LifecycleService() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Coroutine scope for network operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 3 minutes in milliseconds
//    private val UPDATE_INTERVAL_MS = 10000L   //180000L

    private var locationHeartBeatFrequencyInSeconds: Int = 30
    private var token: String = ""

    private var sharedPrefsUtil: SharedPreferencesUtil? = null

    private var serviceStarted = false

    override fun onCreate() {
        super.onCreate()
        sharedPrefsUtil = SharedPreferencesUtil(this)
        isServiceRunning = true
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Define callback for location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    // Location received on main thread loop.
                    // Offload network work to IO thread.
                    sendLocationToServer(location)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (serviceStarted == false) {
            serviceStarted = true
            sharedPrefsUtil = SharedPreferencesUtil(this)
//        sharedPrefsUtil?.saveBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING,true)

            locationHeartBeatFrequencyInSeconds =
                sharedPrefsUtil?.getInt(PrefsKey.LOCATION_HEART_BEAT_FREQUENCY_IN_SECONDS) ?: 30
            token = AppUtils.createBearerToken(
                sharedPrefsUtil?.getString(PrefsKey.USER_ACCESS_TOKEN) ?: ""
            )
            val action = intent?.action
            if (action == ACTION_STOP_SERVICE) {
                stopSelf()
                return START_NOT_STICKY
            }

            // 1. Create Notification Channel (required for API 26+)
            createNotificationChannel()

            // 2. Build the persistent notification
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Trip in Progress")
                .setContentText("Sharing your location with server...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true) // User cannot dismiss
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()


            // 3. Start Foreground BEFORE doing anything else.
            // Android 14 (SDK 34) requires specifying the type here explicitly
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            // 4. Start requesting locations
            startLocationUpdates()
        }else{
            Log.d(TAG, "onStartCommand: Service already started")
        }
        // Restart service if OS kills it due to memory pressure
        return START_STICKY
    }
    private fun getLocationHeartBeatInSeconds(): Long {
        var heartBeatInSeconds =  sharedPrefsUtil?.getInt(PrefsKey.LOCATION_HEART_BEAT_FREQUENCY_IN_SECONDS) ?: 30
        if (heartBeatInSeconds<10){
            heartBeatInSeconds = 10
        }
        return heartBeatInSeconds*1000L
    }

    @SuppressLint("MissingPermission") // Checked in MainActivity before starting service
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, // Good balance for driving
            getLocationHeartBeatInSeconds()
        ).apply {
            // Sets the minimum time interval between updates.
            // Controls how frequently your app receives updates.
            setMinUpdateIntervalMillis(getLocationHeartBeatInSeconds())
            // How much deviation from the interval is acceptable to save power
            setMaxUpdateDelayMillis(getLocationHeartBeatInSeconds() + 60000) // Allow up to 1 min delay for batching
        }.build()

        Log.d(TAG, "Requesting location updates every 3 mins")
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    private fun sendLocationToServer(location: Location) {
        // Use lifecycleScope to launch a coroutine for network operations
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val lat = location.latitude
                val lon = location.longitude
                val locationData =
                    LocationData(latitude = lat.toString(), longitude = lon.toString())
                val response = RetrofitClient.apiService.sendLocation(token, locationData)

                if (response.isSuccessful) {
                    Log.d(LocationPingService.TAG, "Location sent successfully: Lat: $lat, Lon: $lon")
                } else {
                    Log.e(
                        LocationPingService.TAG,
                        "Failed to send location. Code: ${response.code()}, Body: ${response.errorBody()?.string()}"
                    )

                }
            } catch (e: IOException) {
                Log.e(LocationPingService.TAG, "Network error: ${e.message}")
            } catch (e: HttpException) {
                Log.e(LocationPingService.TAG, "HTTP error: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        serviceStarted = false
        // Important: Stop updates when service is destroyed
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Service destroyed, location updates stopped")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Driver Tracking Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }


    companion object {
        private const val TAG = "LocationService"
        private const val CHANNEL_ID = "LocationServiceChannel"
        private const val NOTIFICATION_ID = 12345
        const val ACTION_STOP_SERVICE = "STOP_SERVICE_ACTION"
        var isServiceRunning = false
    }
}