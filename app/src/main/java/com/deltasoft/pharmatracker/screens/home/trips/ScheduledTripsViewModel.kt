package com.deltasoft.pharmatracker.screens.home.trips

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.deltasoft.pharmatracker.screens.home.trips.entity.ScheduledTrip
import com.deltasoft.pharmatracker.screens.home.trips.entity.ScheduledTripsResponse
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "ScheduledTripsViewModel"
class ScheduledTripsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ScheduledTripsRepository(this)

    private val _scheduledTripsState = MutableStateFlow<ScheduledTripsState>(ScheduledTripsState.Idle)
    val scheduledTripsState = _scheduledTripsState.asStateFlow()

    var token = ""

    init {
        val appContext = getApplication<Application>().applicationContext
        val sharedPrefsUtil = SharedPreferencesUtil(appContext)
        token = AppUtils.createBearerToken(sharedPrefsUtil?.getString(PrefsKey.USER_ACCESS_TOKEN)?:"")
    }

    fun getScheduledTripsList() {
        _scheduledTripsState.value = ScheduledTripsState.Loading
        try {
            repository.getScheduledList(token)
        } catch (e: Exception) {
            _scheduledTripsState.value = ScheduledTripsState.Error("Fetch scheduled trips failed: ${e.message}")
        }
    }

    fun updateScheduledListState(code: Int, message: String, scheduleNewTripResponse: ScheduledTripsResponse?= null) {
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

    fun cancelScheduledTrip(tripId: String, context: Context) {
        _cancelScheduleState.value = CancelScheduleState.Loading
        try {
            repository?.cancelScheduledTrip(token,tripId,context)
        } catch (e: Exception) {
            _cancelScheduleState.value = CancelScheduleState.Error("Cancel failed: ${e.message}")
        }
    }


    private val _cancelScheduleState = MutableStateFlow<CancelScheduleState>(CancelScheduleState.Idle)
    val cancelScheduleState = _cancelScheduleState.asStateFlow()
    fun updateCancelScheduleState(message: String,success: Boolean = false) {
        if (success){
            _cancelScheduleState.value = CancelScheduleState.Success(message)
        }else{
            _cancelScheduleState.value = CancelScheduleState.Error(message)
        }
    }

    fun clearCancelScheduleState() {
        _cancelScheduleState.value = CancelScheduleState.Idle
    }
}