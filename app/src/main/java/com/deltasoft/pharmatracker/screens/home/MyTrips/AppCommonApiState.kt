package com.deltasoft.pharmatracker.screens.home.MyTrips

sealed class AppCommonApiState{
    object Idle : AppCommonApiState()
    object Loading : AppCommonApiState()
    data class Success(val message: String): AppCommonApiState()
    data class Error(val message: String) : AppCommonApiState()
}