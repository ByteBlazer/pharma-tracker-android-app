package com.deltasoft.pharmatracker.navigation

object NavConstants {
    // Routes
    const val ROUTE_SPLASH_SCREEN = "splash_screen"
    const val ROUTE_LOGIN_SCREEN = "login_screen"
    const val ROUTE_OTP_VERIFICATION_SCREEN = "otpVerification_screen"
    const val ROUTE_HOME_SCREEN = "home_screen"
    const val ROUTE_PROFILE_SCREEN = "profile_screen"
    const val ROUTE_SCHEDULE_NEW_TRIP_SCREEN = "schedule_new_trip_screen"
    const val ROUTE_SINGLE_TRIP_DETAILS_SCREEN = "single_trip_details_screen"

    /*Home screen bottom navigation routes*/
    const val ROUTE_SCAN_SCREEN = "scan"
    const val ROUTE_ROUTE_SCREEN = "route_queue"
    const val ROUTE_SCHEDULED_TRIPS_SCREEN = "scheduled_trips"
    const val ROUTE_MY_TRIPS_SCREEN = "drive"

    // Arguments
    const val ARG_PHONE_NUMBER = "phoneNumber"
    const val ARG_ROUTE = "routeWithUsers"
    const val ARG_USER_LIST = "userList"
    const val ARG_SELECTED_SCHEDULED_TRIP_ID = "selectedScheduleTripId"

}