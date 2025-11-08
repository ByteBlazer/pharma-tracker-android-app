package com.deltasoft.pharmatracker.screens.home.queue.entity

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class UserSummaryList(
    var scannedByUserId: String? = null,
    var scannedByName: String? = null,
    var scannedFromLocation: String? = null,
    var count: Int? = null,
    var docIdList: ArrayList<String> = arrayListOf(),
    val isChecked: MutableState<Boolean> = mutableStateOf(false)
)
