package com.deltasoft.pharmatracker

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.deltasoft.pharmatracker.screens.home.UserType
import com.deltasoft.pharmatracker.screens.home.trips.ScheduledTripsState
import com.deltasoft.pharmatracker.screens.home.trips.entity.ScheduledTripsResponse
import com.deltasoft.pharmatracker.screens.splash.SplashRepository
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
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
}