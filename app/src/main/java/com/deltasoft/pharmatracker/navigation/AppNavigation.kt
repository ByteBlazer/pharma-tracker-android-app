package com.deltasoft.pharmatracker.navigation


import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deltasoft.pharmatracker.api.AuthEvent
import com.deltasoft.pharmatracker.api.AuthManager
import com.deltasoft.pharmatracker.screens.splash.SplashScreen
import com.deltasoft.pharmatracker.screens.home.HomeScreen
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.SingleTripDetailsScreen
import com.deltasoft.pharmatracker.screens.home.profile.ProfileScreen
import com.deltasoft.pharmatracker.screens.home.route.scheduletrip.ScheduleNewTrip
import com.deltasoft.pharmatracker.screens.login.LoginScreen
import com.deltasoft.pharmatracker.screens.otp.OtpVerificationScreen
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil

@Composable
fun AppNavigation(applicationContext: Context,
                  authManager: AuthManager = AuthManager
) {
    val navController = rememberNavController()


    LaunchedEffect(Unit) {
        authManager.authEvents.collect { event ->
            when (event) {
                is AuthEvent.Expired -> {
                    // Get the current route/destination
                    val currentRoute = navController.currentDestination?.route

                    // 1. Define the routes you want to exclude from forced navigation
                    val protectedRoutes = listOf(Screen.Login.route, Screen.Splash.route,Screen.OtpVerification.route)

                    // 2. Check if the user is already on a protected route
                    if (currentRoute in protectedRoutes) {
                        Log.d("AuthFlow", "User already on a protected route ($currentRoute). Token cleared, but no navigation required.")
                        // Exit the block without navigating
                        return@collect
                    }

                    // If NOT on a protected route, proceed with cleanup and navigation
                    Log.e("AuthFlow", "Session expired. Navigating to Login.")

                    // --- Session Cleanup ---
                    val sharedPrefsUtil = SharedPreferencesUtil(applicationContext)
                    val phone = sharedPrefsUtil.getString(PrefsKey.PHONE_NUMBER)
                    AppUtils.stopService(applicationContext)
                    sharedPrefsUtil.saveString(PrefsKey.USER_ACCESS_TOKEN,"")

                    // --- Navigation ---
                    navController.navigate(Screen.Login.createRoute(phone)) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

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