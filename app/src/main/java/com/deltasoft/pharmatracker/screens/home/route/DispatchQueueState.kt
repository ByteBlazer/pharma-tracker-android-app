package com.deltasoft.pharmatracker.screens.home.route

sealed class DispatchQueueState {
    object Idle : DispatchQueueState()
    object Loading : DispatchQueueState()
    object Success : DispatchQueueState()
    data class Error(val message: String) : DispatchQueueState()
}