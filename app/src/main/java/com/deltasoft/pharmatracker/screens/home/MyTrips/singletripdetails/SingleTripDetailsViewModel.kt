package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.SingleTripDetailsResponse
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class SingleTripDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private var sharedPreferences: SharedPreferences

    private val repository = SingleTripDetailsRepository(this)

    var token = ""

    init {
        val appContext = getApplication<Application>().applicationContext
        val sharedPrefsUtil = SharedPreferencesUtil(appContext)
        sharedPreferences = sharedPrefsUtil.getSharedPreference()
        token =
            AppUtils.createBearerToken(sharedPrefsUtil?.getString(PrefsKey.USER_ACCESS_TOKEN) ?: "")
    }

    private val _singleTripDetailsState = MutableStateFlow<SingleTripDetailsState>(SingleTripDetailsState.Idle)
    val singleTripDetailsState = _singleTripDetailsState.asStateFlow()

    fun getSingleTripDetails(selectedScheduledTripId: String) {
        _singleTripDetailsState.value = SingleTripDetailsState.Loading
        try {
            repository.getSingleTripDetails(token,selectedScheduledTripId)
        } catch (e: Exception) {
            _singleTripDetailsState.value = SingleTripDetailsState.Error("Fetch trip details failed: ${e.message}")
        }
    }

    fun updateScheduledListState(code: Int, message: String, singleTripDetailsResponse: SingleTripDetailsResponse?= null) {
        when(code){
            200->{
                _singleTripDetailsState.value = SingleTripDetailsState.Success(singleTripDetailsResponse?: SingleTripDetailsResponse())
            }
            400->{
                _singleTripDetailsState.value = SingleTripDetailsState.Error(message)
            }
            500->{
                _singleTripDetailsState.value = SingleTripDetailsState.Error(message)
            }
            else->{
                _singleTripDetailsState.value = SingleTripDetailsState.Error(message)
            }
        }
    }
    fun clearState() {
        _singleTripDetailsState.value = SingleTripDetailsState.Idle
    }

    fun endTrip(selectedScheduledTripId: String) {

    }

}