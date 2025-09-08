package com.deltasoft.pharmatracker.navigation


import android.content.Context
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deltasoft.pharmatracker.screens.SplashScreen
import com.deltasoft.pharmatracker.screens.home.HomeScreen
import com.deltasoft.pharmatracker.screens.login.LoginScreen
import com.deltasoft.pharmatracker.screens.otp.OtpVerificationScreen

@Composable
fun AppNavigation(applicationContext: Context) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController,applicationContext)
        }
        composable(Screen.Login.route,
            arguments = listOf(navArgument("phonenumber") { type = NavType.StringType })) {backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phonenumber") ?: ""
            LoginScreen(navController,phoneNumber)
        }
        composable(Screen.OtpVerification.route,
            arguments = listOf(navArgument("phonenumber") { type = NavType.StringType })) {backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phonenumber") ?: ""
            OtpVerificationScreen(navController,phoneNumber)
        }
        composable(Screen.Home.route) {
            HomeScreen(applicationContext)
        }
    }
}