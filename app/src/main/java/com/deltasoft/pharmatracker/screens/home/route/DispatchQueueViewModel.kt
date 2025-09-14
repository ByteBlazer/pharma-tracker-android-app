package com.deltasoft.pharmatracker.screens.home.route

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.deltasoft.pharmatracker.screens.home.route.entity.DispatchItem
import com.deltasoft.pharmatracker.screens.home.route.entity.DispatchQueueResponse
import com.deltasoft.pharmatracker.screens.home.route.entity.RouteSummaryList
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.ArrayList

class DispatchQueueViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DispatchQueueRepository(this)

    private val _dispatchQueueState = MutableStateFlow<DispatchQueueState>(DispatchQueueState.Idle)
    val dispatchQueueState = _dispatchQueueState.asStateFlow()

    var token = ""

    init {
        val appContext = getApplication<Application>().applicationContext
        val sharedPrefsUtil = SharedPreferencesUtil(appContext)
        token = AppUtils.createBearerToken(sharedPrefsUtil?.getString(PrefsKey.USER_ACCESS_TOKEN)?:"")
//        getDispatchQueueList()
    }

    fun getDispatchQueueList() {
        _dispatchQueueState.value = DispatchQueueState.Loading
        try {
            repository.getDispatchQueueList(token)
        } catch (e: Exception) {
            _dispatchQueueState.value = DispatchQueueState.Error("Login failed: ${e.message}")
        }
    }

    fun updateDispatchQueueListState(code: Int, errorMessage: String,dispatchQueueResponse: DispatchQueueResponse? = null){
        when(code){
            200->{
                _dispatchQueueState.value = DispatchQueueState.Success(dispatchQueueResponse?:DispatchQueueResponse())
            }
            400->{
                _dispatchQueueState.value = DispatchQueueState.Error(errorMessage)
            }
            500->{
                _dispatchQueueState.value = DispatchQueueState.Error(errorMessage)
            }
            else->{
                _dispatchQueueState.value = DispatchQueueState.Error(errorMessage)
            }
        }
    }

    fun clearLoginState() {
        _dispatchQueueState.value = DispatchQueueState.Idle
    }
}

