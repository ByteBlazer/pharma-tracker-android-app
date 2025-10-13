package com.deltasoft.pharmatracker.screens.splash

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.deltasoft.pharmatracker.screens.home.schedule.ScheduledTripsState
import com.deltasoft.pharmatracker.screens.home.schedule.entity.ScheduledTripsResponse
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private var sharedPreferences: SharedPreferences
    private var sharedPreferencesUtil: SharedPreferencesUtil

    private val repository = SplashRepository(this)

    private val _scheduledTripsState =
        MutableStateFlow<ScheduledTripsState>(ScheduledTripsState.Idle)
    val scheduledTripsState = _scheduledTripsState.asStateFlow()


    var token = ""

    var apiRetryAttempt = 0

    init {
        val appContext = getApplication<Application>().applicationContext
        sharedPreferencesUtil = SharedPreferencesUtil(appContext)
        sharedPreferences = sharedPreferencesUtil.getSharedPreference()
        token =
            AppUtils.createBearerToken(
                sharedPreferencesUtil?.getString(PrefsKey.USER_ACCESS_TOKEN) ?: ""
            )
    }

    fun getMyTripsList(delay: Long = 0) {
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