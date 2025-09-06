package com.deltasoft.pharmatracker.api

import com.deltasoft.pharmatracker.screens.home.location.LocationData
import com.deltasoft.pharmatracker.screens.login.LoginRequest
import com.deltasoft.pharmatracker.screens.otp.OtpRequestBody
import com.deltasoft.pharmatracker.screens.otp.OtpVerificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("auth/generate-otp")
    suspend fun generateOtp(@Body loginRequest: LoginRequest): Response<Unit>

    @POST("auth/validate-otp")
    suspend fun verifyOtp(@Body otpRequestBody: OtpRequestBody): Response<OtpVerificationResponse>

    @POST("location/register") // Replace with your actual API endpoint
    suspend fun sendLocation(@Header("Authorization") token :String,
        @Body locationData: LocationData): Response<Void>
}