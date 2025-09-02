package com.deltasoft.pharmatracker.screens.login

import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginRepository(var viewModel: LoginViewModel) {
    var viewModelScope = CoroutineScope(Dispatchers.IO)

    fun generateOtp(phoneNumber: String){
        val loginRequest = LoginRequest(phoneNumber)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.generateOtp(loginRequest)
                if (response.isSuccessful) {
                    viewModel.updateLoginState(response.code(),response.message())
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    if (errorBodyString != null) {
                        try {
                            val errorResponse = Gson().fromJson(errorBodyString, ApiResponse::class.java)
                            val errorMessage = errorResponse.message
                            viewModel.updateLoginState(response.code(), errorMessage?:"")
                        } catch (e: Exception) {
                            // Catch JSON parsing errors if the error body format is unexpected
                            println("Failed to parse error body: ${e.message}")
                            viewModel.updateLoginState(0, "${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                println("Network error: ${e.message}")
                viewModel.updateLoginState(0, "${e.message}")
            }
        }
    }
}