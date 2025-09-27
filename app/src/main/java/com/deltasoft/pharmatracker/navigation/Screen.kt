package com.deltasoft.pharmatracker.navigation

sealed class Screen(val route: String) {
    object Splash : Screen(NavConstants.ROUTE_SPLASH_SCREEN)
    object Login : Screen("${NavConstants.ROUTE_LOGIN_SCREEN}/{${NavConstants.ARG_PHONE_NUMBER}}"){
        fun createRoute(phoneNumber: String?) = "${NavConstants.ROUTE_LOGIN_SCREEN}/$phoneNumber"
    }
    object OtpVerification : Screen("${NavConstants.ROUTE_OTP_VERIFICATION_SCREEN}/{${NavConstants.ARG_PHONE_NUMBER}}"){
        fun createRoute(phoneNumber: String?) = "${NavConstants.ROUTE_OTP_VERIFICATION_SCREEN}/$phoneNumber"
    }
    object Home : Screen(NavConstants.ROUTE_HOME_SCREEN)
    object Profile : Screen(NavConstants.ROUTE_PROFILE_SCREEN)
    object ScheduleNewTrip : Screen("${NavConstants.ROUTE_SCHEDULE_NEW_TRIP_SCREEN}/{${NavConstants.ARG_ROUTE}}/{${NavConstants.ARG_USER_LIST}}"){
        fun createRoute(route: String, userList: String) = "${NavConstants.ROUTE_SCHEDULE_NEW_TRIP_SCREEN}/$route/$userList"
    }
}