package com.deltasoft.pharmatracker.screens.home.route.scheduletrip.entity

data class ScheduleNewTripRequest(
    var route:String,
    var userIds:ArrayList<String>,
    var driverId:String,
    var vehicleNbr:String
)