package com.deltasoft.pharmatracker.api

import android.content.Context
import android.util.Log
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil

// 1. Update AuthInterceptor to require Context in its constructor
// (and access it only for utilities like SharedPreferences)

class AuthInterceptor(private val appContext: Context) : okhttp3.Interceptor {

    // Move the token expiry logic inside the interceptor or a dedicated manager
    private fun verifyLocalTokenExpiry(): Boolean {
        // Use the stored appContext here
        val sharedPrefsUtil = SharedPreferencesUtil(appContext)
        val token = sharedPrefsUtil.getString(PrefsKey.USER_ACCESS_TOKEN)

        // Use your existing logic
        if (AppUtils.isValidToken(token)){
            Log.d("AuthInterceptor", "Token still valid.")
            return false
        } else {
            Log.e("AuthInterceptor", "Token expired. Triggering logout.")
            return true
        }
    }

    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401 || response.code == 403 ) {
            val isTokenExpired = verifyLocalTokenExpiry()

            // Only notify for logout if the token is indeed locally invalid/expired
            if (isTokenExpired) {
                AuthManager.notifySessionExpired()
            }
        }
        return response
    }
}
