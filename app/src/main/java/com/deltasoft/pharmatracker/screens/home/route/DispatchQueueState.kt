package com.deltasoft.pharmatracker.screens.home.route

import com.deltasoft.pharmatracker.screens.home.route.entity.DispatchQueueResponse

sealed class DispatchQueueState {
    object Idle : DispatchQueueState()
    object Loading : DispatchQueueState()
    data class Success(val dispatchQueueResponse: DispatchQueueResponse) : DispatchQueueState()
    data class Error(val message: String) : DispatchQueueState()
}