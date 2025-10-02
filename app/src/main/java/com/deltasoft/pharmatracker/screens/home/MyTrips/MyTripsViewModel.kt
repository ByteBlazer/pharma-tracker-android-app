package com.deltasoft.pharmatracker.screens.home.MyTrips

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.deltasoft.pharmatracker.screens.home.location.LocationPingService
import com.deltasoft.pharmatracker.screens.home.schedule.ScheduledTripsState
import com.deltasoft.pharmatracker.screens.home.schedule.entity.ScheduledTrip
import com.deltasoft.pharmatracker.screens.home.schedule.entity.ScheduledTripsResponse
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyTripsViewModel(application: Application) : AndroidViewModel(application) {

    private var sharedPreferences: SharedPreferences
    private var sharedPreferencesUtil: SharedPreferencesUtil

    private val repository = MyTripsRepository(this)

    private val _scheduledTripsState =
        MutableStateFlow<ScheduledTripsState>(ScheduledTripsState.Idle)
    val scheduledTripsState = _scheduledTripsState.asStateFlow()

//    private val _isServiceRunning = MutableStateFlow(false)
//    val isServiceRunning = _isServiceRunning.asStateFlow()

    var token = ""

    init {
        val appContext = getApplication<Application>().applicationContext
        sharedPreferencesUtil = SharedPreferencesUtil(appContext)
        sharedPreferences = sharedPreferencesUtil.getSharedPreference()
        token =
            AppUtils.createBearerToken(sharedPreferencesUtil?.getString(PrefsKey.USER_ACCESS_TOKEN) ?: "")
//        checkServiceRunningStatus()
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
            _startTripState.value = AppCommonApiState.Loading
            try {
                val tripId = currentTrip?.tripId?:0
                repository?.startTrip(token, tripId.toString())
            } catch (e: Exception) {
                _startTripState.value = AppCommonApiState.Error("Cancel failed: ${e.message}")
            }
        }
    }


    private val _startTripState = MutableStateFlow<AppCommonApiState>(AppCommonApiState.Idle)
    val startTripState = _startTripState.asStateFlow()
    fun updateStartTripState(message: String, success: Boolean = false) {
        if (success){
            _startTripState.value = AppCommonApiState.Success(message)
        }else{
            _startTripState.value = AppCommonApiState.Error(message)
        }
    }

    fun clearStartTripState() {
        _startTripState.value = AppCommonApiState.Idle
    }

    var currentTrip :ScheduledTrip? = null

    private val _loading = MutableStateFlow<Boolean>(false)
    val loading = _loading.asStateFlow()
    fun setLoading(value: Boolean) {
        _loading.value = value
    }

//    private val preferenceListener =
//        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
//            if (key == PrefsKey.IS_LOCATION_SERVICE_RUNNING.name) {
//                _isServiceRunning.value =
//                    sharedPreferences.getBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING.name, false)
//            }
//        }
//    fun checkServiceRunningStatus() {
//        // Register the listener
//        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
//        // Set the initial value
//        _isServiceRunning.value = sharedPreferences.getBoolean(PrefsKey.IS_LOCATION_SERVICE_RUNNING.name, false)
//    }

    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude = _longitude.asStateFlow()

    private val locationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationPingService.ACTION_LOCATION_UPDATE) {
                val latitude = intent.getDoubleExtra(LocationPingService.EXTRA_LATITUDE, 0.0)
                val longitude = intent.getDoubleExtra(LocationPingService.EXTRA_LONGITUDE, 0.0)
                viewModelScope.launch {
                    _latitude.value = latitude
                    _longitude.value = longitude
                }
            }
        }
    }

    fun clearLocationValues() {
        _latitude.value = null
        _longitude.value = null
    }

    fun registerReceiver(context: Context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(
            locationUpdateReceiver,
            IntentFilter(LocationPingService.ACTION_LOCATION_UPDATE)
        )
    }

    fun unregisterReceiver(context: Context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(locationUpdateReceiver)
    }

    fun startMyService(context: Context) {
        val serviceIntent = Intent(context, LocationPingService::class.java)
        context.startForegroundService(serviceIntent)
    }

    fun stopService(context: Context) {
        val serviceIntent = Intent(context, LocationPingService::class.java)
        context.stopService(serviceIntent)
    }

    fun storeCurrentTripId() {
        sharedPreferencesUtil?.saveString(PrefsKey.CURRENT_TRIP_ID,(currentTrip?.tripId?:0).toString())
    }

    fun getCurrentTripId():String {
        return sharedPreferencesUtil?.getString(PrefsKey.CURRENT_TRIP_ID,"")?:""
    }

    fun restartForegroundService(context: Context) {
        val serviceIntent = Intent(context, LocationPingService::class.java)

        // A. Stop the service first (This will call onDestroy() in your Service)
        context.stopService(serviceIntent)

        // B. Start the service again immediately (This will call onCreate() and then onStartCommand())
        // Use startForegroundService for a foreground service, especially on newer Android versions (API 26+)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun clearAllValues() {
        currentTrip = null
        _scheduledTripsState.value = ScheduledTripsState.Idle
        _scheduledList.value = arrayListOf()
        _startTripState.value = AppCommonApiState.Idle
        _latitude.value = null
        _longitude.value = null
        _loading.value = false
    }

}