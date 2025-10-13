package com.deltasoft.pharmatracker.screens.home.route.scheduletrip

import com.deltasoft.pharmatracker.screens.home.route.scheduletrip.entity.DriverListResponse

sealed class DriverListState {
    object Idle : DriverListState()
    object Loading : DriverListState()
    data class Success(val driverListResponse: DriverListResponse) : DriverListState()
    data class Error(val message: String) : DriverListState()
}