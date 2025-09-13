package com.deltasoft.pharmatracker.screens.otp

import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.deltasoft.pharmatracker.screens.login.LoginRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OtpVerificationRepository(var viewModel: OtpVerificationViewModel) {

    var viewModelScope = CoroutineScope(Dispatchers.IO)

    fun verifyOtp(phoneNumber: String, otp: String) {
        val otpRequestBody = OtpRequestBody(mobile = phoneNumber, otp = otp)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.verifyOtp(otpRequestBody)
                if (response.isSuccessful) {
                    viewModel.updateOtpVerificationState(
                        code = response.code(),
                        otpVerificationResponse = response.body()
                    )
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateOtpVerificationState(
                                response.code(),
                                errorMessage ?: ""
                            )
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateOtpVerificationState(0, "${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateOtpVerificationState(0, "${e.message}")
            }
        }
    }

    fun resendOTP(phoneNumber: String) {
        val loginRequest = LoginRequest(phoneNumber)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.generateOtp(loginRequest)
                if (response.isSuccessful) {
                } else {

                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
            }
        }
    }
}