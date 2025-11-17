package com.deltasoft.pharmatracker

import android.location.Location
import android.util.Log
import com.deltasoft.pharmatracker.api.ApiResponse
import com.deltasoft.pharmatracker.api.RetrofitClient
import com.deltasoft.pharmatracker.screens.home.location.LocationData
import com.deltasoft.pharmatracker.utils.AppConstants
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivityRepository(var viewModel: MainActivityViewModel) {
    var viewModelScope = CoroutineScope(Dispatchers.IO)
    private  val TAG = "MainActivityRepository"

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
                viewModel.updateMyScheduledListState(0, AppConstants.NETWORK_LOSS_MESSAGE)
            }
        }
    }

    fun sendLocation(
        token: String,
        location: Location,
        sharedPreferencesUtil: SharedPreferencesUtil
    ) {
        viewModelScope.launch {
            try {
                val locationData = LocationData(latitude = location.latitude.toString(), longitude = location.longitude.toString())
                val response = RetrofitClient.apiService.sendLocation(token, locationData)
                if (response.isSuccessful) {
                    Log.d(TAG, "checkAndSendLocationToServer: api success")

                    Log.d(TAG, "checkAndSendLocationToServer: lastLogInTimeInMills updated main repo")
                    sharedPreferencesUtil?.saveLong(PrefsKey.LAST_LOCATION_UPDATE_TIME_IN_MILLS,System.currentTimeMillis())
                } else {
                }
            } catch (e: Exception) {
            }
        }
    }
}