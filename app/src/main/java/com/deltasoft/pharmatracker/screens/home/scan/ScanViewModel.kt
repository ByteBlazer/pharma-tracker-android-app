package com.deltasoft.pharmatracker.screens.home.scan

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "ScanViewModel"
class ScanViewModel(application: Application) : AndroidViewModel(application) {
    init {

    }
    private val repository = ScanRepository(this)
    var token = ""
    init {
        val appContext = getApplication<Application>().applicationContext
        val sharedPrefsUtil = SharedPreferencesUtil(appContext)
        token = AppUtils.createBearerToken(sharedPrefsUtil?.getString(PrefsKey.USER_ACCESS_TOKEN)?:"")
    }

    private val _scanDocState = MutableStateFlow<ScanDocState>(ScanDocState.Idle)
    val scanDocState = _scanDocState.asStateFlow()

    fun scanDoc(barcode: String) {
        if (barcode.isNotNullOrEmpty()) {
            _scanDocState.value = ScanDocState.Loading
            try {
                repository.scanDoc(token = token, barcode = barcode)
            } catch (e: Exception) {
                _scanDocState.value = ScanDocState.Error("Login failed: ${e.message}",0)
            }
        }
    }

    fun updateScanDocState(code: Int, errorMessage: String){
        when(code){
            200->{
                _scanDocState.value = ScanDocState.Success(errorMessage,code)
            }
            400->{
                _scanDocState.value = ScanDocState.Error(errorMessage,code)
            }
            500->{
                _scanDocState.value = ScanDocState.Error(errorMessage,code)
            }
            else->{
                _scanDocState.value = ScanDocState.Error(errorMessage,code)
            }
        }
    }

    fun clearScanDocState() {
        _scanDocState.value = ScanDocState.Idle
    }
}
