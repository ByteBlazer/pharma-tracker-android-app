package com.deltasoft.pharmatracker.utils.sharedpreferences

/**
 * Enum class to manage all preference keys in one place.
 * This prevents typos and centralizes key management.
 */
enum class PrefsKey {
    USER_ACCESS_TOKEN,
    USER_ID,
    USER_NAME,
    IS_LOGGED_IN,
    LAST_LOGIN_TIME,
    PHONE_NUMBER,
    ROLES,
    LOCATION_HEART_BEAT_FREQUENCY_IN_SECONDS,
//    IS_LOCATION_SERVICE_RUNNING,
    CURRENT_TRIP_ID,
    LAST_LOCATION_UPDATE_TIME_IN_MILLS
}