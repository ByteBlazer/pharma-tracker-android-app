package com.deltasoft.pharmatracker.screens.home.trips

import com.deltasoft.pharmatracker.screens.home.trips.entity.ScheduledTripsResponse

sealed class ScheduledTripsState {
    object Idle : ScheduledTripsState()
    object Loading : ScheduledTripsState()
    data class Success(val scheduledTripsResponse: ScheduledTripsResponse) : ScheduledTripsState()
    data class Error(val message: String) : ScheduledTripsState()
}