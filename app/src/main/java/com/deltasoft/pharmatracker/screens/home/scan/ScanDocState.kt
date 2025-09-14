package com.deltasoft.pharmatracker.screens.home.scan

sealed class ScanDocState {
    object Idle : ScanDocState()
    object Loading : ScanDocState()
    object Success : ScanDocState()
    data class Error(val message: String) : ScanDocState()
}