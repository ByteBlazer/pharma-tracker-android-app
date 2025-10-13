package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity

import com.deltasoft.pharmatracker.utils.AppUtils

data class SingleTripDetailsResponse(
    val tripId: Int? = null,
    val createdBy: String? = null,
    val createdById: String? = null,
    val driverName: String? = null,
    val driverId: String? = null,
    val vehicleNumber: String? = null,
    val status: String? = null,
    val route: String? = null,
    val createdAt: String? = null,
    val lastUpdatedAt: String? = null,
    val creatorLocation: String? = null,
    val driverLocation: String? = null,
    val driverLastKnownLatitude: String? = null,
    val driverLastKnownLongitude: String? = null,
    val driverLastLocationUpdateTime: String? = null,
    val docGroups: List<DocGroup>? = null
){
    val createdAtFormatted: String? get() = createdAt?.let { AppUtils.convertIso8601ToIst(it) }
}
