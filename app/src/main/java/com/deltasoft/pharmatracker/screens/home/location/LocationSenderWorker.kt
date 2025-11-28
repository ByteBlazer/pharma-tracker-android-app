package com.deltasoft.pharmatracker.screens.home.location

import com.deltasoft.pharmatracker.api.RetrofitClient
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlin.coroutines.suspendCoroutine


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.deltasoft.pharmatracker.MyApp
import com.google.android.gms.location.*
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationSenderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = "LocationWorker"
    private val LOCATION_PING_WORK_NAME = "LocationPingChain"
    private var DELAY_MINUTES = 1L
    private var sharedPrefsUtil: SharedPreferencesUtil? = null
    private var locationHeartBeatFrequencyInSeconds: Int = 30
    private var token: String = ""

    // Instantiate your API service
    private val apiService = RetrofitClient.apiService


    override suspend fun doWork(): Result {
        Log.d(TAG, "Worker started. Attempting to get location.")

        return try {
            sharedPrefsUtil = SharedPreferencesUtil(applicationContext)
            locationHeartBeatFrequencyInSeconds =
                sharedPrefsUtil?.getInt(PrefsKey.LOCATION_HEART_BEAT_FREQUENCY_IN_SECONDS) ?: 30
            token = AppUtils.createBearerToken(
                sharedPrefsUtil?.getString(PrefsKey.USER_ACCESS_TOKEN) ?: ""
            )
            // 1. Get Location using the suspend bridge
            val location = getCurrentLocationSuspend()
            Log.i(TAG, "Location acquired: Lat=${location.latitude}, Lon=${location.longitude}")

            // 2. Send to Server
            val success = pingAPIWithLocation(location)

            // 3. Schedule the Next Run (Always reschedule to maintain heartbeat)
            scheduleNextWork()

            if (success) Result.success() else Result.retry()

        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission denied in Worker. Stopping chain.", e)
            Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Work failed due to error: ${e.message}", e)
            scheduleNextWork()
            Result.retry()
        }
    }

    private suspend fun getCurrentLocationSuspend(): Location = suspendCoroutine { continuation ->

        // Pass the application context to the fetching function
        LocationServiceUtils.fetchCurrentLocation(
            context = applicationContext,
            onSuccess = { location ->
                // Resume the coroutine with the successful result
                continuation.resume(location)
            },
            onFailure = { exception ->
                // Resume the coroutine with the failure
                continuation.resumeWithException(exception)
            }
        )
    }

    // --- API PING IMPLEMENTATION ---
    private suspend fun pingAPIWithLocation(location: Location): Boolean {
        val lat = location.latitude
        val lon = location.longitude

        return try {
            val locationData = LocationData(latitude = lat.toString(), longitude = lon.toString())

            // Retrofit call is made via the suspend function
            val response = apiService.sendLocation(token, locationData)

            if (response.isSuccessful) {
                MyApp.logToDataDog("✅ ${getLoggerPrependDate()} Worker Location api success")
                Log.d(TAG, "API Success: Location sent successfully.")
                true
            } else {
                MyApp.logToDataDog("❌ ${getLoggerPrependDate()} Worker Location Api Fail")
                Log.e(TAG, "API Failure: Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network Error: Check connectivity.", e)
            false
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP Error: Server returned an exception.", e)
            false
        }
    }
    private fun getLoggerPrependDate():String{
        val userName = sharedPrefsUtil?.getString(PrefsKey.USER_NAME)?:""
        val userId = sharedPrefsUtil?.getString(PrefsKey.USER_ID)?:""
        return "$userName($userId)"
    }

    // --- RESCHEDULING LOGIC ---
    private fun scheduleNextWork() {
        val nextWorkRequest = OneTimeWorkRequest.Builder(LocationSenderWorker::class.java)
            .setInitialDelay(DELAY_MINUTES, TimeUnit.MINUTES)
            .addTag(LOCATION_PING_WORK_NAME)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(nextWorkRequest)
        Log.d(TAG, "Scheduled next run in $DELAY_MINUTES minutes.")
    }
}