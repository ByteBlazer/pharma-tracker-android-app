package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails

import android.location.Location
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.SingleTripDetailsResponse

sealed class LocationState {
    object Idle : LocationState()
    object Loading : LocationState()
    data class Success(val location: Location) : LocationState()
    data class Error(val message: String) : LocationState()
}