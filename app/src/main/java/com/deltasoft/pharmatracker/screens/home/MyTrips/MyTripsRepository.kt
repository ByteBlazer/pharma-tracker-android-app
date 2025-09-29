package com.deltasoft.pharmatracker.screens.home.MyTrips

import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyTripsRepository(var viewModel: MyTripsViewModel) {
    var viewModelScope = CoroutineScope(Dispatchers.IO)

    fun getMyTripsList(token: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getMyTripsList(token)
                if (response.isSuccessful) {
                    viewModel.updateMyScheduledListState(response.code(), response.message(),response?.body())
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateMyScheduledListState(response.code(), errorMessage ?: "")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateMyScheduledListState(0, "${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateMyScheduledListState(0, "${e.message}")
            }
        }
    }



    fun startTrip(token: String, tripId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.startTrip(token, tripId)
                if (response.isSuccessful) {
                    viewModel.updateStartTripState(response?.body()?.message?:"",true)
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateStartTripState(errorMessage ?: "")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateStartTripState("${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateStartTripState("${e.message}")
            }
        }
    }
}