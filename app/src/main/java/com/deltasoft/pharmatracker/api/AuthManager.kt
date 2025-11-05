package com.deltasoft.pharmatracker.api

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Define this class in your dependency injection (e.g., Dagger/Hilt or simple object)
object AuthManager {
    // SharedFlow is perfect for sending events that multiple collectors (like your NavHost)
    // might need to observe.
    private val _authEvents = MutableSharedFlow<AuthEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val authEvents = _authEvents.asSharedFlow()

    // This is the function called by the Interceptor
    fun notifySessionExpired() {
        // Ensure this logic is only run once per expiry event if possible
        _authEvents.tryEmit(AuthEvent.Expired)
    }
}

// Sealed class to represent all possible auth events
sealed class AuthEvent {
    object Expired : AuthEvent()
    // object TokenRefreshed : AuthEvent() // for future use
}