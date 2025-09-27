package com.deltasoft.pharmatracker.screens.home.schedule

import com.deltasoft.pharmatracker.screens.home.schedule.entity.ScheduledTripsResponse

sealed class CancelScheduleState {
    object Idle : CancelScheduleState()
    object Loading : CancelScheduleState()
    data class Success(val message: String): CancelScheduleState()
    data class Error(val message: String) : CancelScheduleState()
}