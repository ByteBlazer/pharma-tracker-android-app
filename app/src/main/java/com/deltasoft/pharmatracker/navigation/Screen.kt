package com.deltasoft.pharmatracker.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen/{phonenumber}"){
        fun createRoute(phoneNumber: String) = "login_screen/$phoneNumber"
    }
    object OtpVerification : Screen("otpVerification_screen/{phonenumber}"){
        fun createRoute(phoneNumber: String) = "otpVerification_screen/$phoneNumber"
    }
    object Home : Screen("home_screen")
    object Profile : Screen("profile_screen")
}