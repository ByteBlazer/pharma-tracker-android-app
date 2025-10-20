package com.deltasoft.pharmatracker.screens.home.trips

sealed class CancelScheduleState {
    object Idle : CancelScheduleState()
    object Loading : CancelScheduleState()
    data class Success(val message: String): CancelScheduleState()
    data class Error(val message: String) : CancelScheduleState()
}