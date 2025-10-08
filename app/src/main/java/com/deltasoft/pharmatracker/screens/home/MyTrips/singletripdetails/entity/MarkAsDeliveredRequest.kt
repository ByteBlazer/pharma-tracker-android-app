package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity

data class MarkAsDeliveredRequest(
    var signature : String? = null,
    var deliveryComment : String? = null,
    var deliveryLatitude : Double ? = null,
    var deliveryLongitude : Double ? = null
)
