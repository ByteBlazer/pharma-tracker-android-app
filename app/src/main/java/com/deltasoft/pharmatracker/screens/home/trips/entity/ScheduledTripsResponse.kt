package com.deltasoft.pharmatracker.screens.home.trips.entity

data class ScheduledTripsResponse(
    var success: Boolean? = false,
    var message: String? = null,
    var totalTrips: Int? = null,
    var trips: ArrayList<ScheduledTrip> = arrayListOf(),
    var totalDocs: Int? = null,
    var statusCode : Int? = null
)
