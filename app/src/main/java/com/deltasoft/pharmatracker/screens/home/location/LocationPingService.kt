package com.deltasoft.pharmatracker.screens.home.location

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.deltasoft.pharmatracker.MyApp
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LocationPingService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Coroutine scope for network operations
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var locationHeartBeatFrequencyInSeconds: Int = 30
    private var token: String = ""

    private var sharedPrefsUtil: SharedPreferencesUtil? = null

    private var serviceStarted = false

    companion object {
        const val CHANNEL_ID = "APIPingServiceChannel"
        const val TAG = "APIPingService"
        const val ACTION_LOCATION_UPDATE = "com.deltasoft.pharmatracker.LOCATION_UPDATE"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        var isServiceRunning = false
    }

    override fun onCreate() {
        super.onCreate()

        sharedPrefsUtil = SharedPreferencesUtil(this)
        isServiceRunning = true
        createNotificationChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val isLocationAllowed = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        MyApp.logToDataDog("✅ ${getLoggerPrependDate()} Location Ping Service started")
        MyApp.logToDataDog("✅ ${getLoggerPrependDate()} Pi Ping Service started isDeviceLocationOn ${AppUtils.isDeviceLocationOn(applicationContext)}")
        MyApp.logToDataDog("✅ ${getLoggerPrependDate()} Pi Ping Service started isLocationAllowed $isLocationAllowed")
    }

    private fun getLoggerPrependDate():String{
        val userName = sharedPrefsUtil?.getString(PrefsKey.USER_NAME)?:""
        val userId = sharedPrefsUtil?.getString(PrefsKey.USER_ID)?:""
        return "$userName($userId)"
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (serviceStarted == false) {
            serviceStarted = true
            sharedPrefsUtil = SharedPreferencesUtil(this)
//        sharedPrefsUtil?.saveBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING,true)

            locationHeartBeatFrequencyInSeconds =
                sharedPrefsUtil?.getInt(PrefsKey.LOCATION_HEART_BEAT_FREQUENCY_IN_SECONDS) ?: 30
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
                    } else {
                        Log.d(TAG, "run: invalid token")
                    }
                    handler.postDelayed(
                        this,
                        (getLocationHeartBeatInSeconds(applicationContext) * 1000).toLong()
                    )
                }
            }
            handler.post(runnable)
//            handler.postDelayed(runnable,(getLocationHeartBeatInSeconds(applicationContext) * 1000).toLong())
        } else {
            Log.d(TAG, "onStartCommand: Service already started")
        }

        return START_STICKY
    }

    private fun getLocationHeartBeatInSeconds(applicationContext: Context?): Int {
        return sharedPrefsUtil?.getInt(PrefsKey.LOCATION_HEART_BEAT_FREQUENCY_IN_SECONDS) ?: 30
    }

    private fun requestSingleLocationUpdate() {
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

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    Log.d(TAG, "Fetched location: $it")

                    MyApp.logToDataDog("✅ ${getLoggerPrependDate()} Fetch Location Success latitude: ${location.latitude}, longitude ${location.longitude}")
                    pingAPIWithLocation(it) { success ->
                        if (success) {
                            Log.d(TAG, "checkAndSendLocationToServer: lastLogInTimeInMills updated locationping service")
                            sharedPrefsUtil?.saveLong(PrefsKey.LAST_LOCATION_UPDATE_TIME_IN_MILLS,System.currentTimeMillis())
                            MyApp.logToDataDog("✅ ${getLoggerPrependDate()} Send Location Api Success")
                            // Send location via broadcast
                            val broadcastIntent = Intent(ACTION_LOCATION_UPDATE).apply {
                                putExtra(EXTRA_LATITUDE, it.latitude)
                                putExtra(EXTRA_LONGITUDE, it.longitude)
                            }
                            LocalBroadcastManager.getInstance(this@LocationPingService)
                                .sendBroadcast(broadcastIntent)
                            Log.d(TAG, "API Ping successful")
                        } else {
                            Log.e(TAG, "API Ping failed")
                            MyApp.logToDataDog("❌ ${getLoggerPrependDate()} Send Location Api Fail")
                        }
                    }
                } ?: Log.e(TAG, "getCurrentLocation returned a null location.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get current location", e)
                MyApp.logToDataDog("❌ ${getLoggerPrependDate()} Fetch Location Fail ${e.message}")
            }
    }

    // Modify the signature to include the callback
    private fun pingAPIWithLocation(location: Location, onResult: (success: Boolean) -> Unit) {
        val lat = location.latitude
        val lon = location.longitude

        serviceScope.launch {
            try {
                val locationData =
                    LocationData(latitude = lat.toString(), longitude = lon.toString())
                val response = RetrofitClient.apiService.sendLocation(token, locationData)

                if (response.isSuccessful) {
                    Log.d(TAG, "Location sent successfully: Lat: $lat, Lon: $lon")
                    onResult(true) // Invoke callback with true for success
                } else {
                    Log.e(
                        TAG,
                        "Failed to send location. Code: ${response.code()}, Body: ${response.errorBody()?.string()}"
                    )
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
        val isLocationAllowed = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        MyApp.logToDataDog("❌ ${getLoggerPrependDate()} Pi Ping Service Stopped")
        MyApp.logToDataDog("❌ ${getLoggerPrependDate()} Pi Ping Service Stopped isDeviceLocationOn ${AppUtils.isDeviceLocationOn(applicationContext)}")
        MyApp.logToDataDog("❌ ${getLoggerPrependDate()} Pi Ping Service Stopped isLocationAllowed $isLocationAllowed")
        isServiceRunning = false
        serviceStarted = false
        Log.d(TAG, "onDestroy: ")
        if (::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
        serviceScope.cancel() // Cancel all coroutines when the service is destroyed
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
//        Log.w(TAG, "System Event: App swiped from Recents. Scheduling immediate restart via AlarmManager.")

//        // 1. Create the intent for the broadcast receiver
//        val restartServiceIntent = Intent(applicationContext, ServiceRestartReceiver::class.java).apply {
//            action = "ACTION_RESTART_SERVICE"
//        }
//
//        // 2. Schedule the restart using AlarmManager (more reliable than relying only on START_STICKY)
//        val pendingIntent = PendingIntent.getBroadcast(
//            this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        )
//        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        // Schedule to fire after 1 second
//        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, pendingIntent)

        super.onTaskRemoved(rootIntent)
//        Log.v(TAG, "System Event: onTaskRemoved() finished. Process will now be terminated by OS.")
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