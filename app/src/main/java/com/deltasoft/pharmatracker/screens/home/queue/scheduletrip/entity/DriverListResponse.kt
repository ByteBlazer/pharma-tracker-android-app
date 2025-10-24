package com.deltasoft.pharmatracker.screens.home.queue.scheduletrip.entity

data class DriverListResponse(
    var success: Boolean? = false,
    var message: String? = null,
    var statusCode : Int? = null,
    var drivers    : ArrayList<Driver> = arrayListOf(),
)