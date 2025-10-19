package com.deltasoft.pharmatracker.screens.home.queue.scheduletrip.entity

data class Driver(
    val userId : String? = null,
    val driverName : String? = null,
    val vehicleNumber : String? = null,
    val baseLocationName : String? = null,
    val sameLocation : Boolean? = null,
    val self : Boolean? = null
)
