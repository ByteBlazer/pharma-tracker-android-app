package com.deltasoft.pharmatracker.screens.home.route.entity

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.util.UUID
import kotlin.uuid.Uuid

data class UserSummaryList(
    var scannedByUserId: String? = null,
    var scannedByName: String? = null,
    var scannedFromLocation: String? = null,
    var count: Int? = null,
    val isChecked: MutableState<Boolean> = mutableStateOf(false)
)
