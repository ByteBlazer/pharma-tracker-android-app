package com.deltasoft.pharmatracker.screens.home.MyTrips

import com.deltasoft.pharmatracker.screens.home.schedule.CancelScheduleState

sealed class StartTripState{
    object Idle : StartTripState()
    object Loading : StartTripState()
    data class Success(val message: String): StartTripState()
    data class Error(val message: String) : StartTripState()
}