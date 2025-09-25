package com.deltasoft.pharmatracker.screens.home.schedule

import android.content.Context
import android.widget.Toast
import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                viewModel.updateScheduledListState(0, "${e.message}")
            }
        }
    }

    fun cancelScheduledTrip(token: String, tripId: String, context: Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.cancelScheduledTrip(token,tripId)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, response.body()?.message?:"", Toast.LENGTH_LONG).show()
                    }
                    viewModel.getScheduledTripsList()
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message

                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, errorMessage?:"", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, e.message?:"", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, e.message?:"", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}