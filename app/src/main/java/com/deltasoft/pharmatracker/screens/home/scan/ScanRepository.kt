package com.deltasoft.pharmatracker.screens.home.scan

import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScanRepository(var viewModel: ScanViewModel) {
    var viewModelScope = CoroutineScope(Dispatchers.IO)

    fun scanDoc(token : String, barcode: String){

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.scanDoc(token = token, barcode = barcode)
                if (response.isSuccessful) {
                    viewModel.updateScanDocState(response.code(),response.message())
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
                viewModel.updateScanDocState(0, "${e.message}")
            }
        }
    }
}