package com.deltasoft.pharmatracker.screens.home.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude = _longitude.asStateFlow()


    private val locationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == APIPingService.ACTION_LOCATION_UPDATE) {
                val latitude = intent.getDoubleExtra(APIPingService.EXTRA_LATITUDE, 0.0)
                val longitude = intent.getDoubleExtra(APIPingService.EXTRA_LONGITUDE, 0.0)

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
            IntentFilter("com.example.fullcomposeapp.LOCATION_UPDATE")
        )
    }

    fun unregisterReceiver(context: Context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(locationUpdateReceiver)
    }
}