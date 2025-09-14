package com.deltasoft.pharmatracker.screens.home.route

import android.util.Log
import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "DispatchQueueRepository"
class DispatchQueueRepository(var viewModel: DispatchQueueViewModel) {
    var viewModelScope = CoroutineScope(Dispatchers.IO)

    fun getDispatchQueueList(token: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getDispatchQueueList(token)
                Log.d(TAG, "getDispatchQueueList: "+response.body().toString())
                if (response.isSuccessful) {
                    viewModel.updateDispatchQueueListState(response.code(), response.message())
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse =
                                Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateDispatchQueueListState(response.code(), errorMessage ?: "")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateDispatchQueueListState(0, "${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateDispatchQueueListState(0, "${e.message}")
            }
        }
    }
}