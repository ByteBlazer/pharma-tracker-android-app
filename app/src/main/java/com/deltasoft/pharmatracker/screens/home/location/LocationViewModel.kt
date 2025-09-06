package com.deltasoft.pharmatracker.screens.home.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
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
            if (intent?.action == LocationPingService.ACTION_LOCATION_UPDATE) {
                val latitude = intent.getDoubleExtra(LocationPingService.EXTRA_LATITUDE, 0.0)
                val longitude = intent.getDoubleExtra(LocationPingService.EXTRA_LONGITUDE, 0.0)
                viewModelScope.launch {
                    _latitude.value = latitude
                    _longitude.value = longitude
                }
            }
        }
    }

    fun registerReceiver(context: Context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(
            locationUpdateReceiver,
            IntentFilter(LocationPingService.ACTION_LOCATION_UPDATE)
        )
    }

    fun unregisterReceiver(context: Context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(locationUpdateReceiver)
    }

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning = _isServiceRunning.asStateFlow()

    private lateinit var sharedPreferences: SharedPreferences

    private val preferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == PrefsKey.IS_LOCATION_SERVICE_RUNNING.name) {
                _isServiceRunning.value =
                    sharedPreferences.getBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING.name, false)
            }
        }

    fun initialize(context: Context) {
        sharedPreferences = SharedPreferencesUtil(context).getSharedPreference()
        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
        // Set the initial value
        _isServiceRunning.value = sharedPreferences.getBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING.name, false)
    }

    fun clearLocationValues() {
        _latitude.value = null
        _longitude.value = null
    }
}