package com.deltasoft.pharmatracker.screens.home.trips

import android.content.Context
import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.deltasoft.pharmatracker.utils.AppConstants
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "ScheduledTripsRepositor"
class ScheduledTripsRepository(var viewModel: ScheduledTripsViewModel) {
    var viewModelScope = CoroutineScope(Dispatchers.IO)

    fun getScheduledList(token: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getScheduledList(token)
                if (response.isSuccessful) {
                    viewModel.updateScheduledListState(response.code(), response.message(),response?.body())
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateScheduledListState(response.code(), errorMessage ?: "")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateScheduledListState(0, "${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateScheduledListState(0, AppConstants.NETWORK_LOSS_MESSAGE)
            }
        }
    }

    fun cancelScheduledTrip(token: String, tripId: String, context: Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.cancelScheduledTrip(token,tripId)
                if (response.isSuccessful) {
                    viewModel.updateCancelScheduleState(response?.body()?.message?:"",true)
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateCancelScheduleState(errorMessage ?: "")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateCancelScheduleState("${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateCancelScheduleState(AppConstants.NETWORK_LOSS_MESSAGE)
            }
        }
    }
}