package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails

import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.MarkAsDeliveredRequest
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.MarkAsUnDeliveredRequest
import com.deltasoft.pharmatracker.utils.AppConstants
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SingleTripDetailsRepository(var viewModel: SingleTripDetailsViewModel) {
    var viewModelScope = CoroutineScope(Dispatchers.IO)

    fun getSingleTripDetails(token: String, selectedScheduledTripId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getSingleTripDetails(token = token, tripId = selectedScheduledTripId)
                if (response.isSuccessful) {
                    viewModel.updateScheduledListState(
                        response.code(),
                        response.message(),
                        response?.body()
                    )
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateScheduledListState(
                                response.code(),
                                errorMessage ?: ""
                            )
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
    fun dropOffTrip(token: String, tripId: String, heading: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.dropOff(token = token, tripId = tripId, heading = heading)
                if (response.isSuccessful) {
                    viewModel.updateDropOffTripState(response?.body()?.message?:"",true)
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateDropOffTripState(errorMessage ?: "")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateDropOffTripState("${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateDropOffTripState(AppConstants.NETWORK_LOSS_MESSAGE)
            }
        }
    }
    fun endTrip(token: String, tripId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.endTrip(token, tripId)
                if (response.isSuccessful) {
                    viewModel.updateEndTripState(response?.body()?.message?:"",true)
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateEndTripState(errorMessage ?: "")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateEndTripState("${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateEndTripState(AppConstants.NETWORK_LOSS_MESSAGE)
            }
        }
    }


    fun markAsDelivered(
        token: String,
        docId: String,
        markAsDeliveredRequest: MarkAsDeliveredRequest,
        updateCustomerLocation : Boolean
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.markAsDelivered(token = token, docId = docId, body = markAsDeliveredRequest,updateCustomerLocation = updateCustomerLocation)
                if (response.isSuccessful) {
                    viewModel.updateMarkAsDeliveredStateState(response?.body()?.message?:"",true)
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateMarkAsDeliveredStateState(errorMessage ?: "")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateMarkAsDeliveredStateState("${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateMarkAsDeliveredStateState(AppConstants.NETWORK_LOSS_MESSAGE)
            }
        }
    }


    fun markAsUnDelivered(token: String, docId: String, markAsUnDeliveredRequest: MarkAsUnDeliveredRequest) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.markAsUnDelivered(token = token, docId = docId, body = markAsUnDeliveredRequest)
                if (response.isSuccessful) {
                    viewModel.updateMarkAsUnDeliveredStateState(response?.body()?.message?:"",true)
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateMarkAsUnDeliveredStateState(errorMessage ?: "")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateMarkAsUnDeliveredStateState("${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateMarkAsUnDeliveredStateState(AppConstants.NETWORK_LOSS_MESSAGE)
            }
        }
    }
}