package com.deltasoft.pharmatracker.screens.otp


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.jwtdecode.JwtDecodeUtil
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OtpVerificationViewModel(application: Application) :  AndroidViewModel(application) {

    private val sharedPrefsUtil = SharedPreferencesUtil(application)

    private val repository = OtpVerificationRepository(this)

    private val _otpVerificationState = MutableStateFlow<OtpVerificationState>(OtpVerificationState.Idle)
    val otpVerificationState = _otpVerificationState.asStateFlow()

    fun verifyOtp(phoneNumber: String, otp: String) {
        viewModelScope.launch {
            _otpVerificationState.value = OtpVerificationState.Loading
            try {
                repository.verifyOtp(phoneNumber, otp)
            } catch (e: Exception) {
                _otpVerificationState.value =
                    OtpVerificationState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun updateOtpVerificationState(code: Int, errorMessage: String="",otpVerificationResponse: OtpVerificationResponse?=null) {
        when(code){
            200->{
                sharedPrefsUtil.saveString(PrefsKey.USER_ACCESS_TOKEN,otpVerificationResponse?.access_token?:"")
                AppUtils.storePayLoadDetailsToSharedPreferences(sharedPrefsUtil,otpVerificationResponse?.access_token?:"")
                Log.d("TAG", "updateOtpVerificationState: "+sharedPrefsUtil.getString(PrefsKey.USER_ACCESS_TOKEN))
                _otpVerificationState.value = OtpVerificationState.Success
            }
            400->{
                _otpVerificationState.value = OtpVerificationState.Error(errorMessage)
            }
            500->{
                _otpVerificationState.value = OtpVerificationState.Error(errorMessage)
            }
            else->{
                _otpVerificationState.value = OtpVerificationState.Error(errorMessage)
            }
        }

    }

    fun onResendClick(phoneNumber: String) {
        repository.resendOTP(phoneNumber)
    }
}
