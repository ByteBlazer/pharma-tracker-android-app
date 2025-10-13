package com.deltasoft.pharmatracker.screens.otp

sealed class OtpVerificationState {
    object Idle : OtpVerificationState()
    object Loading : OtpVerificationState()
    object Success : OtpVerificationState()
    data class Error(val message: String) : OtpVerificationState()
}