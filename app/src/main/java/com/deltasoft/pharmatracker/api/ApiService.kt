package com.deltasoft.pharmatracker.api

import com.deltasoft.pharmatracker.screens.home.location.LocationData
import com.deltasoft.pharmatracker.screens.home.route.entity.DispatchQueueResponse
import com.deltasoft.pharmatracker.screens.login.LoginRequest
import com.deltasoft.pharmatracker.screens.otp.OtpRequestBody
import com.deltasoft.pharmatracker.screens.otp.OtpVerificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("auth/generate-otp")
    suspend fun generateOtp(@Body loginRequest: LoginRequest): Response<Unit>

    @POST("auth/validate-otp")
    suspend fun verifyOtp(@Body otpRequestBody: OtpRequestBody): Response<OtpVerificationResponse>

    @POST("location/register") // Replace with your actual API endpoint
    suspend fun sendLocation(
        @Header("Authorization") token: String,
        @Body locationData: LocationData
    ): Response<Void>

    @GET("doc/dispatch-queue")
    suspend fun getDispatchQueueList(
        @Header("Authorization") token: String
    ): Response<DispatchQueueResponse>

    @POST("doc/scan-and-add/{barcode}")
    suspend fun scanDoc(
        @Header("Authorization") token: String,
        @Path("barcode") barcode: String,
    ): Response<ApiResponse>
}