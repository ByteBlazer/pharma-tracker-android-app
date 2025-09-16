package com.deltasoft.pharmatracker.screens.home.scan

import com.deltasoft.pharmatracker.api.ApiResponse

sealed class ScanDocState {
    object Idle : ScanDocState()
    object Loading : ScanDocState()
    data class Success(val message: String, val code : Int) : ScanDocState()
    data class Error(val message: String,val code: Int) : ScanDocState()
}