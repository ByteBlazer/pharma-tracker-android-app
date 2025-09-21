package com.deltasoft.pharmatracker.screens.home.route.scheduletrip

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.deltasoft.pharmatracker.screens.home.route.scheduletrip.entity.Driver
import com.deltasoft.pharmatracker.screens.home.route.scheduletrip.entity.DriverListResponse
import com.deltasoft.pharmatracker.screens.home.route.scheduletrip.entity.ScheduleNewTripResponse
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "ScanViewModel"
class ScheduleNewTripViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ScheduleNewTripRepository(this)
    var token = ""

    private val _driverListState = MutableStateFlow<DriverListState>(DriverListState.Idle)
    val driverListState = _driverListState.asStateFlow()

    init {
        val appContext = getApplication<Application>().applicationContext
        val sharedPrefsUtil = SharedPreferencesUtil(appContext)
        token = AppUtils.createBearerToken(sharedPrefsUtil?.getString(PrefsKey.USER_ACCESS_TOKEN)?:"")

        getDriverList()
    }


    fun getDriverList() {
        _driverListState.value = DriverListState.Loading
        try {
            repository.getDriverList(token = token)
        } catch (e: Exception) {
            _driverListState.value = DriverListState.Error("Login failed: ${e.message}")
        }
    }

    fun updateDriverListState(code: Int, errorMessage: String,driverListResponse: DriverListResponse?=null){
        when(code){
            200->{
                _driverListState.value = DriverListState.Success(driverListResponse?:DriverListResponse())
            }
            400->{
                _driverListState.value = DriverListState.Error(errorMessage)
            }
            500->{
                _driverListState.value = DriverListState.Error(errorMessage)
            }
            else->{
                _driverListState.value = DriverListState.Error(errorMessage)
            }
        }
    }

    fun clearDriverListState() {
        _driverListState.value = DriverListState.Idle
    }


    private val _scheduleNewTripState = MutableStateFlow<ScheduleNewTripState>(ScheduleNewTripState.Idle)
    val scheduleNewTripState = _scheduleNewTripState.asStateFlow()

    fun scheduleNewTrip() {
        _scheduleNewTripState.value = ScheduleNewTripState.Loading
        try {
            repository.scheduleNewTrip(token = token)
        } catch (e: Exception) {
            _scheduleNewTripState.value = ScheduleNewTripState.Error("Login failed: ${e.message}")
        }
    }

    fun updateScheduleNewTripState(code: Int, errorMessage: String,scheduleNewTripResponse: ScheduleNewTripResponse? = null){
        when(code){
            200->{
                _scheduleNewTripState.value = ScheduleNewTripState.Success(scheduleNewTripResponse?:ScheduleNewTripResponse())
            }
            400->{
                _scheduleNewTripState.value = ScheduleNewTripState.Error(errorMessage)
            }
            500->{
                _scheduleNewTripState.value = ScheduleNewTripState.Error(errorMessage)
            }
            else->{
                _scheduleNewTripState.value = ScheduleNewTripState.Error(errorMessage)
            }
        }
    }

    fun clearScheduleNewTripState() {
        _scheduleNewTripState.value = ScheduleNewTripState.Idle
    }

    private val _driverList = MutableStateFlow<ArrayList<Driver>>(arrayListOf())
    val driverList = _driverList.asStateFlow()

    fun updateDriverList(drivers: ArrayList<Driver>) {
        _driverList.value = drivers
    }
    private val _selectedDriver = MutableStateFlow<String>("")
    val selectedDriver = _selectedDriver.asStateFlow()
    fun updateSelectedDriver(driverId: String, clear: Boolean) {
        _selectedDriver.value = if (clear ) "" else driverId
    }
}
