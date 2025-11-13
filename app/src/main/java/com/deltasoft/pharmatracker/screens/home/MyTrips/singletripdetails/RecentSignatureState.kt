package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails

import android.location.Location
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.RecentSignatureResponse

sealed class RecentSignatureState {
    object Idle : RecentSignatureState()
    object Loading : RecentSignatureState()
    data class Success(val signature: RecentSignatureResponse) : RecentSignatureState()
    data class Error(val message: String) : RecentSignatureState()
}