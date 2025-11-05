package com.deltasoft.pharmatracker.api

import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.MarkAsDeliveredRequest
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.MarkAsUnDeliveredRequest
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.SingleTripDetailsResponse
import com.deltasoft.pharmatracker.screens.home.location.LocationData
import com.deltasoft.pharmatracker.screens.home.queue.entity.DispatchQueueResponse
import com.deltasoft.pharmatracker.screens.home.queue.scheduletrip.entity.DriverListResponse
import com.deltasoft.pharmatracker.screens.home.queue.scheduletrip.entity.ScheduleNewTripRequest
import com.deltasoft.pharmatracker.screens.home.queue.scheduletrip.entity.ScheduleNewTripResponse
import com.deltasoft.pharmatracker.screens.home.scan.ScanDocSuccessResponse
import com.deltasoft.pharmatracker.screens.home.trips.entity.ScheduledTripsResponse
import com.deltasoft.pharmatracker.screens.login.LoginRequest
import com.deltasoft.pharmatracker.screens.otp.OtpRequestBody
import com.deltasoft.pharmatracker.screens.otp.OtpVerificationResponse
import com.deltasoft.pharmatracker.utils.AppUtils
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/generate-otp")
    suspend fun generateOtp(
        @Query("appCode") appCode : String,
        @Body loginRequest: LoginRequest,): Response<Unit>

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
    ): Response<ScanDocSuccessResponse>

    @GET("trip/available-drivers")
    suspend fun getDriverList(
        @Header("Authorization") token: String
    ): Response<DriverListResponse>

    @POST("trip")
    suspend fun scheduleNewTrip(
        @Header("Authorization") token: String,
        @Body scheduleNewTripRequest: ScheduleNewTripRequest,
    ): Response<ScheduleNewTripResponse>

    @GET("trip/scheduled-trips-same-location")
    suspend fun getScheduledList(
        @Header("Authorization") token: String
    ): Response<ScheduledTripsResponse>

    @POST("trip/cancel/{tripId}")
    suspend fun cancelScheduledTrip(
        @Header("Authorization") token: String,
        @Path("tripId") tripId: String,
    ): Response<ApiResponse>

    @GET("trip/my-trips")
    suspend fun getMyTripsList(
        @Header("Authorization") token: String
    ): Response<ScheduledTripsResponse>

    @POST("trip/start/{tripId}")
    suspend fun startTrip(
        @Header("Authorization") token: String,
        @Path("tripId") tripId: String,
    ): Response<ApiResponse>

    @GET("trip/{tripId}")
    suspend fun getSingleTripDetails(
        @Header("Authorization") token: String,
        @Path("tripId") tripId: String,
    ): Response<SingleTripDetailsResponse>

    @POST("trip/drop-off-lot/{tripId}/{heading}")
    suspend fun dropOff(
        @Header("Authorization") token: String,
        @Path("tripId") tripId: String,
        @Path("heading") heading : String,
    ): Response<ApiResponse>

    @POST("trip/end/{tripId}")
    suspend fun endTrip(
        @Header("Authorization") token: String,
        @Path("tripId") tripId: String
    ): Response<ApiResponse>

    @PUT("doc/mark-delivery/{docId}")
    suspend fun markAsDelivered(
        @Header("Authorization") token: String,
        @Path("docId") docId: String,
        @Body body : MarkAsDeliveredRequest,
        @Query("updateCustomerLocation") updateCustomerLocation : Boolean
    ): Response<ApiResponse>

    @PUT("doc/mark-delivery-failed/{docId}")
    suspend fun markAsUnDelivered(
        @Header("Authorization") token: String,
        @Path("docId") docId: String,
        @Body body : MarkAsUnDeliveredRequest
    ): Response<ApiResponse>

}