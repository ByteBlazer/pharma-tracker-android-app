package com.deltasoft.pharmatracker.api

import com.deltasoft.pharmatracker.screens.login.LoginRequest
import com.deltasoft.pharmatracker.screens.otp.OtpRequestBody
import com.deltasoft.pharmatracker.screens.otp.OtpVerificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/generate-otp")
    suspend fun generateOtp(@Body loginRequest: LoginRequest): Response<Unit>

    @POST("auth/validate-otp")
    suspend fun verifyOtp(@Body otpRequestBody: OtpRequestBody): Response<OtpVerificationResponse>
}