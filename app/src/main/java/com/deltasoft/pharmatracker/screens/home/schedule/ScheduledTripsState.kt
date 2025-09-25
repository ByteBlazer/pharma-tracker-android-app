package com.deltasoft.pharmatracker.screens.home.schedule

import com.deltasoft.pharmatracker.screens.home.schedule.entity.ScheduledTripsResponse

sealed class ScheduledTripsState {
    object Idle : ScheduledTripsState()
    object Loading : ScheduledTripsState()
    data class Success(val scheduledTripsResponse: ScheduledTripsResponse) : ScheduledTripsState()
    data class Error(val message: String) : ScheduledTripsState()
}