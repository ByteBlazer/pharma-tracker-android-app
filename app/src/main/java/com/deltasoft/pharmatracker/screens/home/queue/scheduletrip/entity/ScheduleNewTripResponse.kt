package com.deltasoft.pharmatracker.screens.home.queue.scheduletrip.entity

data class ScheduleNewTripResponse(
    var success: Boolean? = false,
    var message: String? = null,
    var statusCode : Int? = null,
    var tripId : Int? = null,
    var documentsLoaded : Int? = null,
    var route: String? = null,
    var driverId:String? = null,
    var vehicleNumber:String? = null,
)
