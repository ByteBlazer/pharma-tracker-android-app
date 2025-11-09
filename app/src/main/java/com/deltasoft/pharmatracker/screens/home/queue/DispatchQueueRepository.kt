package com.deltasoft.pharmatracker.screens.home.queue

import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.deltasoft.pharmatracker.utils.AppConstants
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
                if (response.isSuccessful) {
                    viewModel.updateDispatchQueueListState(response.code(), response.message(),response?.body())
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
                viewModel.updateDispatchQueueListState(0, AppConstants.NETWORK_LOSS_MESSAGE)
            }
        }
    }

    fun scanDoc(token: String, barcode: String, unscan: Boolean){

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.scanDoc(token = token, barcode = barcode, unscan = unscan)
                if (response.isSuccessful) {
                    viewModel.updateScanDocState(response.code(),response.body()?.message?:"")
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse = Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateScanDocState(response.code(), errorMessage?:"")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateScanDocState(0, "${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateScanDocState(0, AppConstants.NETWORK_LOSS_MESSAGE)
            }
        }
    }
}