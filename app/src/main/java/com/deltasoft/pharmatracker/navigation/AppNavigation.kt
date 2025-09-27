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
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.SingleTripDetailsScreen
import com.deltasoft.pharmatracker.screens.home.profile.ProfileScreen
import com.deltasoft.pharmatracker.screens.home.route.scheduletrip.ScheduleNewTrip
import com.deltasoft.pharmatracker.screens.login.LoginScreen
import com.deltasoft.pharmatracker.screens.otp.OtpVerificationScreen

@Composable
fun AppNavigation(applicationContext: Context) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController,applicationContext)
        }
        composable(
            route = Screen.Login.route,
            arguments = listOf(navArgument(NavConstants.ARG_PHONE_NUMBER) {
                type = NavType.StringType
                nullable = true // This is the key change!
                defaultValue = null
            })
        ) {backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString(NavConstants.ARG_PHONE_NUMBER)
            LoginScreen(navController, phoneNumber)
        }
        composable(Screen.OtpVerification.route,
            arguments = listOf(navArgument(NavConstants.ARG_PHONE_NUMBER) { type = NavType.StringType })) {backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString(NavConstants.ARG_PHONE_NUMBER) ?: ""
            OtpVerificationScreen(navController,phoneNumber)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController,applicationContext)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController,applicationContext)
        }
        composable(
            route = Screen.ScheduleNewTrip.route,
            arguments = listOf(
                navArgument(NavConstants.ARG_ROUTE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val route = backStackEntry.arguments?.getString(NavConstants.ARG_ROUTE)
            val userListJson = backStackEntry.arguments?.getString(NavConstants.ARG_USER_LIST)

            if (route != null && userListJson != null) {
                ScheduleNewTrip(navController=navController,route = route,userListJson = userListJson)
            }
        }
        composable(
            route = Screen.SingleTripDetails.route,
            arguments = listOf(
                navArgument(NavConstants.ARG_SELECTED_SCHEDULED_TRIP_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val selectedScheduledTripId = backStackEntry.arguments?.getString(NavConstants.ARG_SELECTED_SCHEDULED_TRIP_ID)

            if (selectedScheduledTripId != null) {
                SingleTripDetailsScreen(navController=navController,selectedScheduledTripId = selectedScheduledTripId)
            }
        }
    }
}