package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.deltasoft.pharmatracker.screens.home.MyTrips.AppCommonApiState
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.MarkAsDeliveredRequest
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.MarkAsUnDeliveredRequest
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.SingleTripDetailsResponse
import com.deltasoft.pharmatracker.screens.home.location.LocationPingService
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

import android.location.Location
import android.util.Log
import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.State
import androidx.lifecycle.application


class SingleTripDetailsViewModel(application: Application) : AndroidViewModel(application) {

    var selectedScheduledTripId: String = ""
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

    fun getSingleTripDetails() {
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


    fun dropOffTrip(selectedScheduledTripId: String,heading:String) {
        _dropOffTripState.value = AppCommonApiState.Loading
        try {
            repository.dropOffTrip(token,selectedScheduledTripId,heading)
        } catch (e: Exception) {
            _dropOffTripState.value = AppCommonApiState.Error("Drop Off trip failed: ${e.message}")
        }
    }
    private val _dropOffTripState = MutableStateFlow<AppCommonApiState>(AppCommonApiState.Idle)
    val dropOffTripState = _dropOffTripState.asStateFlow()

    fun updateDropOffTripState(message: String, success: Boolean = false) {
        if (success){
            _dropOffTripState.value = AppCommonApiState.Success(message)
        }else{
            _dropOffTripState.value = AppCommonApiState.Error(message)
        }
    }

    fun clearStartTripState() {
        _dropOffTripState.value = AppCommonApiState.Idle
    }

    fun endTrip(selectedScheduledTripId: String) {
        _endTripState.value = AppCommonApiState.Loading
        try {
            repository.endTrip(token,selectedScheduledTripId)
        } catch (e: Exception) {
            _endTripState.value = AppCommonApiState.Error("End trip failed: ${e.message}")
        }

    }

    private val _endTripState = MutableStateFlow<AppCommonApiState>(AppCommonApiState.Idle)
    val endTripState = _endTripState.asStateFlow()
    fun updateEndTripState(message: String, success: Boolean = false) {
        if (success){
            _endTripState.value = AppCommonApiState.Success(message)
        }else{
            _endTripState.value = AppCommonApiState.Error(message)
        }
    }

    fun clearEndTripState() {
        _endTripState.value = AppCommonApiState.Idle
    }



    private val _markAsDeliveredState = MutableStateFlow<AppCommonApiState>(AppCommonApiState.Idle)
    val markAsDeliveredState = _markAsDeliveredState.asStateFlow()

    fun markAsDelivered(docId: String,signatureEncodedString: String,deliveryComment:String? = null) {
        _markAsDeliveredState.value = AppCommonApiState.Loading
        try {
            requestLocation(docId,signatureEncodedString,deliveryComment)

        } catch (e: Exception) {
            _markAsDeliveredState.value = AppCommonApiState.Error("Mark as delivered failed: ${e.message}")
        }
    }
    /**
     * Triggers the location fetch and handles the result via callbacks.
     */
    fun requestLocation(docId: String,signatureEncodedString: String,deliveryComment:String? = null) {
        val context = application.applicationContext
        AppUtils.fetchCurrentLocation(
            context = context,
            onSuccess = { location ->
                Log.d("LocationVM", "Location error: latitude ${location.latitude} longitude ${location.longitude}")
                val markAsDeliveredRequest = MarkAsDeliveredRequest(signature = signatureEncodedString,deliveryComment = deliveryComment,deliveryLatitude = (location?.latitude?:0).toLong(),deliveryLongitude = (location?.longitude?:0).toLong())
                repository.markAsDelivered(token = token, docId = docId,markAsDeliveredRequest = markAsDeliveredRequest )
            },
            onFailure = { exception ->
                // Handle failure
                Log.e("LocationVM", "Location error: ${exception.message}")
                _markAsDeliveredState.value = AppCommonApiState.Error("Mark as delivered failed: Location error ${exception.message}")
            }
        )
    }

    fun updateMarkAsDeliveredStateState(message: String, success: Boolean = false) {
        if (success){
            _markAsDeliveredState.value = AppCommonApiState.Success(message)
        }else{
            _markAsDeliveredState.value = AppCommonApiState.Error(message)
        }
    }

    fun clearMarkAsDeliveredStateState() {
        _markAsDeliveredState.value = AppCommonApiState.Idle
    }

    private val _markAsUnDeliveredState = MutableStateFlow<AppCommonApiState>(AppCommonApiState.Idle)
    val markAsUnDeliveredState = _markAsUnDeliveredState.asStateFlow()


    fun markAsUnDelivered(docId: String,comment:String) {
        _markAsUnDeliveredState.value = AppCommonApiState.Loading
        try {
            val markAsUnDeliveredRequest = MarkAsUnDeliveredRequest(comment)
            repository.markAsUnDelivered(token = token, docId = docId, markAsUnDeliveredRequest = markAsUnDeliveredRequest)
        } catch (e: Exception) {
            _markAsUnDeliveredState.value = AppCommonApiState.Error("Mark as un delivered failed: ${e.message}")
        }

    }
    fun updateMarkAsUnDeliveredStateState(message: String, success: Boolean = false) {
        if (success){
            _markAsUnDeliveredState.value = AppCommonApiState.Success(message)
        }else{
            _markAsUnDeliveredState.value = AppCommonApiState.Error(message)
        }
    }

    fun clearMarkAsUnDeliveredStateState() {
        _markAsUnDeliveredState.value = AppCommonApiState.Idle
    }

    fun stopService(context: Context) {
        val serviceIntent = Intent(context, LocationPingService::class.java)
        context.stopService(serviceIntent)
    }
}