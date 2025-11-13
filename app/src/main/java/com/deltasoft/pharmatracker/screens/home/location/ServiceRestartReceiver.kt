package com.deltasoft.pharmatracker.screens.home.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class ServiceRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SRR", "Broadcast received. Restarting LocationTrackingService.")

        // Use ContextCompat.startForegroundService to ensure Android O+ compatibility
        val serviceIntent = Intent(context, LocationPingService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}