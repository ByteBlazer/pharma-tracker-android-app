package com.deltasoft.pharmatracker.screens.home.trips.entity

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
    var startedAt : String? = null,
    var lastUpdatedAt : String? = null,
    var creatorLocation : String? = null,
    var driverLocation : String? = null,
    var driverLastKnownLatitude : String? = null,
    var driverLastKnownLongitude : String? = null,
    var driverLastLocationUpdateTime : String? = null,
    var pendingDirectDeliveries : Int? = null,
    var totalDirectDeliveries : Int? = null,
    var deliveryCountStatusMsg : String? = null,
    var pendingLotDropOffs : Int? = null,
    var dropOffCountStatusMsg : String? = null,
){
    val createdAtFormatted: String? get() = createdAt?.let { AppUtils.convertIso8601ToIst(it) }
}