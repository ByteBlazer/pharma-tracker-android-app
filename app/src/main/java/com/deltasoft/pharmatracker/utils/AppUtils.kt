package com.deltasoft.pharmatracker.utils

import android.util.Log
import com.deltasoft.pharmatracker.screens.home.UserType
import com.deltasoft.pharmatracker.utils.jwtdecode.JwtDecodeUtil
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil

import java.util.Date

private const val TAG = "AppUtils"
object AppUtils {
    fun isValidToken(token: String): Boolean {
        if (token.isNotNullOrEmpty()){
            val decodedPayload = JwtDecodeUtil.decodeJwtPayload(token)
            if (decodedPayload != null) {
                Log.d(TAG,"Successfully decoded JWT payload:")
                Log.d(TAG,"ID: ${decodedPayload.id}")
                Log.d(TAG,"Username: ${decodedPayload.username}")
                Log.d(TAG,"Mobile: ${decodedPayload.mobile}")

                // Convert the Unix timestamps to a readable Date format
                val issuedAtDate = Date(decodedPayload.iat * 1000)
                val expirationDate = Date(decodedPayload.exp * 1000)

                Log.d(TAG,"Issued At: $issuedAtDate")
                Log.d(TAG,"Expires At: $expirationDate")

                // Get the current time in milliseconds and compare it to the expiration time.
                // The 'exp' field is in seconds, so we multiply by 1000 to convert to milliseconds.
                val currentTimeMillis = System.currentTimeMillis()
                val expirationTimeMillis = decodedPayload.exp * 1000L

                return currentTimeMillis < expirationTimeMillis
            } else {
                Log.d(TAG,"Failed to decode the provided JWT token.")
                return false
            }
        }else{
            //token is null or empty
            Log.d(TAG,"Jwt token is null or empty")
            return false
        }
    }

    fun String?.isNotNullOrEmpty(): Boolean {
        return !this.isNullOrEmpty()
    }

    fun storePayLoadDetailsToSharedPreferences(
        sharedPrefsUtil: SharedPreferencesUtil,
        token: String
    ) {
        val tokenPayload = JwtDecodeUtil.decodeJwtPayload(token?:"")
        sharedPrefsUtil.saveString(PrefsKey.USER_NAME,tokenPayload?.username?:"")
        sharedPrefsUtil.saveString(PrefsKey.USER_ID,tokenPayload?.id?:"")
        sharedPrefsUtil.saveString(PrefsKey.PHONE_NUMBER,tokenPayload?.mobile?:"")
        sharedPrefsUtil.saveString(PrefsKey.ROLES,tokenPayload?.roles?:"")
//        sharedPrefsUtil.saveString(PrefsKey.ROLES,"")
        sharedPrefsUtil.saveInt(PrefsKey.LOCATION_HEART_BEAT_FREQUENCY_IN_SECONDS,tokenPayload?.locationHeartBeatFrequencyInSeconds?:0)
    }

    fun createBearerToken(accessToken: String): String {
        return "Bearer $accessToken"
    }
}