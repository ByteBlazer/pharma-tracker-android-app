package com.deltasoft.pharmatracker.screens.home.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.deltasoft.pharmatracker.screens.home.location.APIPingService.Companion
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "LocationViewModel"
class LocationViewModel : ViewModel() {
    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude = _longitude.asStateFlow()


    private val locationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: ")
            if (intent?.action == APIPingService.ACTION_LOCATION_UPDATE) {
                val latitude = intent.getDoubleExtra(APIPingService.EXTRA_LATITUDE, 0.0)
                val longitude = intent.getDoubleExtra(APIPingService.EXTRA_LONGITUDE, 0.0)

                Log.d(TAG, "onReceive: latitude "+latitude)
                Log.d(TAG, "onReceive: longitude "+longitude)
                viewModelScope.launch {
                    _latitude.value = latitude
                    _longitude.value = longitude
                }
            }
        }
    }

    fun registerReceiver(context: Context) {
        Log.d(TAG, "registerReceiver: ")
        LocalBroadcastManager.getInstance(context).registerReceiver(
            locationUpdateReceiver,
            IntentFilter(APIPingService.ACTION_LOCATION_UPDATE)
        )
    }

    fun unregisterReceiver(context: Context) {
        Log.d(TAG, "unregisterReceiver: ")
        LocalBroadcastManager.getInstance(context).unregisterReceiver(locationUpdateReceiver)
    }

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning = _isServiceRunning.asStateFlow()

    fun checkServiceStatus(context: Context) {
        val sharedPrefsUtil = SharedPreferencesUtil(context)
        viewModelScope.launch {
            delay(250)
            _isServiceRunning.value = sharedPrefsUtil.getBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING)
        }
    }

    fun stopService() {
        _latitude.value = null
        _longitude.value = null
    }
}