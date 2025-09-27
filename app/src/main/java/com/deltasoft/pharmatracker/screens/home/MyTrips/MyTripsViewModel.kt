package com.deltasoft.pharmatracker.screens.home.MyTrips

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.deltasoft.pharmatracker.screens.home.schedule.ScheduledTripsState
import com.deltasoft.pharmatracker.screens.home.schedule.entity.ScheduledTrip
import com.deltasoft.pharmatracker.screens.home.schedule.entity.ScheduledTripsResponse
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MyTripsViewModel(application: Application) : AndroidViewModel(application) {

    private var sharedPreferences: SharedPreferences

    private val repository = MyTripsRepository(this)

    private val _scheduledTripsState =
        MutableStateFlow<ScheduledTripsState>(ScheduledTripsState.Idle)
    val scheduledTripsState = _scheduledTripsState.asStateFlow()

    var token = ""

    init {
        val appContext = getApplication<Application>().applicationContext
        val sharedPrefsUtil = SharedPreferencesUtil(appContext)
        sharedPreferences = sharedPrefsUtil.getSharedPreference()
        token =
            AppUtils.createBearerToken(sharedPrefsUtil?.getString(PrefsKey.USER_ACCESS_TOKEN) ?: "")
        checkServiceRunningStatus()
    }

    fun getMyTripsList() {
        _scheduledTripsState.value = ScheduledTripsState.Loading
        try {
            repository.getMyTripsList(token)
        } catch (e: Exception) {
            _scheduledTripsState.value = ScheduledTripsState.Error("Fetch My scheduled trips failed: ${e.message}")
        }
    }

    fun updateMyScheduledListState(code: Int, message: String, scheduleNewTripResponse: ScheduledTripsResponse?= null) {
        when(code){
            200->{
                _scheduledTripsState.value = ScheduledTripsState.Success(scheduleNewTripResponse?: ScheduledTripsResponse())
            }
            400->{
                _scheduledTripsState.value = ScheduledTripsState.Error(message)
            }
            500->{
                _scheduledTripsState.value = ScheduledTripsState.Error(message)
            }
            else->{
                _scheduledTripsState.value = ScheduledTripsState.Error(message)
            }
        }
    }
    fun clearState() {
        _scheduledTripsState.value = ScheduledTripsState.Idle
    }

    private val _scheduledList = MutableStateFlow<ArrayList<ScheduledTrip>>(arrayListOf())
    val scheduledTripList = _scheduledList.asStateFlow()

    fun updateScheduledList(scheduledTripListNew: ArrayList<ScheduledTrip>){
        _scheduledList.value = scheduledTripListNew
    }


    fun startTrip() {
        if (currentTrip?.tripId != null) {
            _startTripState.value = StartTripState.Loading
            try {
                val tripId = currentTrip?.tripId?:0
                repository?.startTrip(token, tripId.toString())
            } catch (e: Exception) {
                _startTripState.value = StartTripState.Error("Cancel failed: ${e.message}")
            }
        }
    }


    private val _startTripState = MutableStateFlow<StartTripState>(StartTripState.Idle)
    val startTripState = _startTripState.asStateFlow()
    fun updateStartTripState(message: String, success: Boolean = false) {
        if (success){
            _startTripState.value = StartTripState.Success(message)
        }else{
            _startTripState.value = StartTripState.Error(message)
        }
    }

    fun clearStartTripState() {
        _startTripState.value = StartTripState.Idle
    }

    var currentTrip :ScheduledTrip? = null

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning = _isServiceRunning.asStateFlow()

    private val preferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == PrefsKey.IS_LOCATION_SERVICE_RUNNING.name) {
                _isServiceRunning.value =
                    sharedPreferences.getBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING.name, false)
            }
        }
    fun checkServiceRunningStatus() {
        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
        // Set the initial value
        _isServiceRunning.value = sharedPreferences.getBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING.name, false)
    }

}