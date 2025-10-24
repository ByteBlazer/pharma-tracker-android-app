package com.deltasoft.pharmatracker.screens.home.queue.entity

data class UserDetails(
    var scannedByUserId: String? = null,
    var scannedByName: String? = null,
    var scannedFromLocation: String? = null,
    var count: Int? = null,
)
