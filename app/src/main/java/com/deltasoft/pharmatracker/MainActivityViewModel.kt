package com.deltasoft.pharmatracker

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.deltasoft.pharmatracker.screens.home.trips.ScheduledTripsState
import com.deltasoft.pharmatracker.screens.home.trips.entity.ScheduledTripsResponse
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MainActivityViewModel"
    private var sharedPreferences: SharedPreferences
    private var sharedPreferencesUtil: SharedPreferencesUtil

    private val repository = MainActivityRepository(this)

    var token = ""

    var apiRetryAttempt = 0

    init {
        val appContext = getApplication<Application>().applicationContext
        sharedPreferencesUtil = SharedPreferencesUtil(appContext)
        sharedPreferences = sharedPreferencesUtil.getSharedPreference()

    }

    private val _lastLogInTimeInMills = MutableStateFlow<Long?>(null)
    val lastLogInTimeInMills = _lastLogInTimeInMills.asStateFlow()

    fun setLastLogInTimeInMills(timeInMills: Long?) {
        _lastLogInTimeInMills.value = timeInMills
    }

    private val _scheduledTripsState =
        MutableStateFlow<ScheduledTripsState>(ScheduledTripsState.Idle)
    val scheduledTripsState = _scheduledTripsState.asStateFlow()

    fun clearScheduledTripsState(){
        _scheduledTripsState.value = ScheduledTripsState.Idle
    }

    fun getMyTripsList(delay: Long = 0) {
        token =
            AppUtils.createBearerToken(
                sharedPreferencesUtil?.getString(PrefsKey.USER_ACCESS_TOKEN) ?: ""
            )
        _scheduledTripsState.value = ScheduledTripsState.Loading
        try {
            repository.getMyTripsList(token,delay)
        } catch (e: Exception) {
            _scheduledTripsState.value =
                ScheduledTripsState.Error("Fetch My scheduled trips failed: ${e.message}")
        }
    }

    fun updateMyScheduledListState(
        code: Int,
        message: String,
        scheduleNewTripResponse: ScheduledTripsResponse? = null
    ) {
        when (code) {
            200 -> {
                _scheduledTripsState.value =
                    ScheduledTripsState.Success(scheduleNewTripResponse ?: ScheduledTripsResponse())
            }

            400 -> {
                _scheduledTripsState.value = ScheduledTripsState.Error(message)
            }

            500 -> {
                _scheduledTripsState.value = ScheduledTripsState.Error(message)
            }

            else -> {
                _scheduledTripsState.value = ScheduledTripsState.Error(message)
            }
        }
    }

    fun clearState() {
        _scheduledTripsState.value = ScheduledTripsState.Idle
    }

    private val _checkBatteryOptimizationClickEvent = MutableStateFlow<UUID?>(null)
    val checkBatteryOptimizationClickEvent: StateFlow<UUID?> = _checkBatteryOptimizationClickEvent.asStateFlow()

    fun onCheckBatteryOptimizationClickEvent() {
        _checkBatteryOptimizationClickEvent.value = UUID.randomUUID()
    }

    fun checkAndSendLocationToServer(tag:String=""){
        Log.d(TAG, "checkAndSendLocationToServer: $tag")
        val lastLogInTimeInMills = sharedPreferencesUtil.getLong(PrefsKey.LAST_LOCATION_UPDATE_TIME_IN_MILLS, defaultValue = System.currentTimeMillis())
        val currentTimeInMills = System.currentTimeMillis()
        token =
            AppUtils.createBearerToken(
                sharedPreferencesUtil?.getString(PrefsKey.USER_ACCESS_TOKEN) ?: ""
            )

        Log.d(TAG, "checkAndSendLocationToServer: lastLogInTimeInMills $lastLogInTimeInMills")
        if (((currentTimeInMills-lastLogInTimeInMills) > 30000) && AppUtils.isValidToken(sharedPreferencesUtil?.getString(PrefsKey.USER_ACCESS_TOKEN) ?: "")){
            Log.d(TAG, "checkAndSendLocationToServer: verify success ${(currentTimeInMills-lastLogInTimeInMills)}")
            sendLocation()
        }else{
            Log.d(TAG, "checkAndSendLocationToServer: verify failed ${(currentTimeInMills-lastLogInTimeInMills)}")
        }
    }

    fun sendLocation() {
        val context = application.applicationContext
        try {
            AppUtils.fetchCurrentLocation(
                context = context,
                onSuccess = { location ->
                    Log.d(TAG, "checkAndSendLocationToServer: location fetch success")
                    repository?.sendLocation(token = token, location = location,sharedPreferencesUtil)
                },
                onFailure = { exception ->
                }
            )
        } catch (e: Exception) {

        }
    }
}