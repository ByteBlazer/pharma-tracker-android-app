package com.deltasoft.pharmatracker.screens.home.route.scheduletrip

import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.deltasoft.pharmatracker.screens.home.route.scheduletrip.entity.ScheduleNewTripRequest
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleNewTripRepository(var viewModel: ScheduleNewTripViewModel) {
    var viewModelScope = CoroutineScope(Dispatchers.IO)

    fun getDriverList(token: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getDriverList(token)
                if (response.isSuccessful) {
                    viewModel.updateDriverListState(response.code(), response.message(),response?.body())
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateDriverListState(response.code(), errorMessage ?: "")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateDriverListState(0, "${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateDriverListState(0, "${e.message}")
            }
        }
    }

    fun scheduleNewTrip(token: String, scheduleNewTripRequest: ScheduleNewTripRequest) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.scheduleNewTrip(token, scheduleNewTripRequest)
                if (response.isSuccessful) {
                    viewModel.updateScheduleNewTripState(response.code(), response.message(),response?.body())
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateScheduleNewTripState(response.code(), errorMessage ?: "")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateScheduleNewTripState(0, "${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateScheduleNewTripState(0, "${e.message}")
            }
        }
    }
}