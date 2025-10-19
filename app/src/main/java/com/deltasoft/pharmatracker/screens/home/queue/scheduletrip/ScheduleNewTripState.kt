package com.deltasoft.pharmatracker.screens.home.queue.scheduletrip

import com.deltasoft.pharmatracker.screens.home.queue.scheduletrip.entity.ScheduleNewTripResponse

sealed class ScheduleNewTripState {
    object Idle : ScheduleNewTripState()
    object Loading : ScheduleNewTripState()
    data class Success(val scheduleNewTripResponse: ScheduleNewTripResponse) : ScheduleNewTripState()
    data class Error(val message: String) : ScheduleNewTripState()
}