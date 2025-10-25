package com.deltasoft.pharmatracker.screens.home.trips.entity

data class ScheduledTripsResponse(
    var success: Boolean? = false,
    var message: String? = null,
    var trips: ArrayList<ScheduledTrip> = arrayListOf(),
    var totalTrips: Int? = null,
    var totalDocs: Int? = null,
    var statusCode : Int? = null
)
