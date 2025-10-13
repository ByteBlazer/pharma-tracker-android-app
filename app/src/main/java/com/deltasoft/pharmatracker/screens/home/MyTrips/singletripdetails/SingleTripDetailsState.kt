package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails

import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.SingleTripDetailsResponse

sealed class SingleTripDetailsState {
    object Idle : SingleTripDetailsState()
    object Loading : SingleTripDetailsState()
    data class Success(val singleTripDetailsResponse: SingleTripDetailsResponse) : SingleTripDetailsState()
    data class Error(val message: String) : SingleTripDetailsState()
}