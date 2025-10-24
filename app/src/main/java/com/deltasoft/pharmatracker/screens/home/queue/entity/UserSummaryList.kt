package com.deltasoft.pharmatracker.screens.home.queue.entity

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class UserSummaryList(
    var scannedByUserId: String? = null,
    var scannedByName: String? = null,
    var scannedFromLocation: String? = null,
    var count: Int? = null,
    val isChecked: MutableState<Boolean> = mutableStateOf(false)
)
