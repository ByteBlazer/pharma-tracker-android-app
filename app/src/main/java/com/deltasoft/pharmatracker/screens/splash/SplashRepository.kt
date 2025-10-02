package com.deltasoft.pharmatracker.screens.splash

import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



class SplashRepository(var viewModel: SplashViewModel) {
    var viewModelScope = CoroutineScope(Dispatchers.IO)

    fun getMyTripsList(token: String, delay: Long) {
        viewModelScope.launch {
            try {
                delay(delay)
                val response = RetrofitClient.apiService.getMyTripsList(token)
                if (response.isSuccessful) {
                    viewModel.updateMyScheduledListState(
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
                            viewModel.updateMyScheduledListState(
                                response.code(),
                                errorMessage ?: ""
                            )
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

}