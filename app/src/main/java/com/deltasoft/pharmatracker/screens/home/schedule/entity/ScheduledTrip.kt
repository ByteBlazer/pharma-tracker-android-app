package com.deltasoft.pharmatracker.screens.home.schedule.entity

import com.deltasoft.pharmatracker.utils.AppUtils

data class ScheduledTrip(
    var tripId : Int? = null,
    var createdBy : String? = null,
    var createdById : String? = null,
    var driverName : String? = null,
    var driverId : String? = null,
    var vehicleNumber : String? = null,
    var status : String? = null,
    var route : String? = null,
    var createdAt : String? = null,
    var lastUpdatedAt : String? = null,
    var creatorLocation : String? = null,
    var driverLocation : String? = null,
){
    val createdAtFormatted: String? get() = createdAt?.let { AppUtils.convertIso8601ToIst(it) }
}