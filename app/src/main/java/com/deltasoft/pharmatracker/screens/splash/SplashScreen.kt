package com.deltasoft.pharmatracker.screens.splash


import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.navigation.Screen
import com.deltasoft.pharmatracker.screens.home.trips.ScheduledTripsState
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

private const val TAG = "SplashScreen"
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SplashScreen(navController: NavHostController, context: Context,
                 splashViewModel: SplashViewModel = viewModel()
) {
    val sharedPrefsUtil = SharedPreferencesUtil(context)
    val token = sharedPrefsUtil.getString(PrefsKey.USER_ACCESS_TOKEN)
    val phoneNumber = sharedPrefsUtil.getString(PrefsKey.PHONE_NUMBER)

    val apiState by splashViewModel.scheduledTripsState.collectAsState()

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(apiState) {
        when (apiState) {
            is ScheduledTripsState.Idle -> {
                Log.d(TAG, "State: Idle")
            }
            is ScheduledTripsState.Loading -> {
                Log.d(TAG, "State: Loading")
            }
            is ScheduledTripsState.Success -> {
                val scheduledTripsResponse =
                    (apiState as ScheduledTripsState.Success).scheduledTripsResponse
                val anyTripIsCurrentlyActive =
                    scheduledTripsResponse?.trips?.any { it?.status.equals("STARTED") }?:false
                Log.d(TAG, "SplashScreen: anyTripIsCurrentlyActive $anyTripIsCurrentlyActive")
                if (anyTripIsCurrentlyActive){
                    AppUtils.restartForegroundService(context)
                }
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) {
                        inclusive = true
                    }
                }
            }
            is ScheduledTripsState.Error -> {
                val message = (apiState as ScheduledTripsState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                splashViewModel.clearState()

                Log.d(TAG, "SplashScreen: splashViewModel.apiRetryAttempt "+splashViewModel.apiRetryAttempt)
                if (splashViewModel.apiRetryAttempt <= 5) {
                    splashViewModel.apiRetryAttempt += 1
                    splashViewModel.getMyTripsList(delay = 1000)
                }else{
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }


    LaunchedEffect(key1 = true) {
        if (AppUtils.isValidToken(token)){
            AppUtils.storePayLoadDetailsToSharedPreferences(sharedPrefsUtil,token)
            if (locationPermissionState.status.isGranted) {
                // If permission granted call api to check any trip is currently active
                splashViewModel.getMyTripsList()
            } else {
                delay(1000)
                // permission not granted so directly move to home
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) {
                        inclusive = true
                    }
                }
            }
        }else {
            val phn = if(phoneNumber.isNotNullOrEmpty()) phoneNumber else null
            navController.navigate(Screen.Login.createRoute(phn)) {
                popUpTo(Screen.Splash
                    .route) {
                    inclusive = true
                }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition()

    // Animate a value from 0f to 1f over time
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        val imageSize = maxWidth * 0.5f// Define the size once for consistency
        val containerWidth = maxWidth.value

        // Calculate the movement range from a negative value (off-screen left)
        // to a positive value (off-screen right)
        val movementRange = containerWidth + imageSize.value

        // The x-offset starts at -imageSize and moves across the screen
        val xOffset = (-imageSize.value + (movementRange * animationProgress)).dp

        Image(
            painter = painterResource(id = R.drawable.ic_delivery_truck),
            contentDescription = "Animated delivery truck",
            modifier = Modifier
                .size(imageSize)
                .offset(x = xOffset),
            contentScale = ContentScale.Fit,
        )
    }
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
////        Text(text = "Splash Screen", fontSize = 24.sp)
//        Image(
//            painter = painterResource(id = R.drawable.ic_delivery_truck),
//            modifier = Modifier
//                .fillMaxWidth(fraction = 0.5f)
//                .aspectRatio(1.0f),
//            contentDescription = "Splash image",
//            contentScale = ContentScale.Fit,
//            colorFilter = ColorFilter.tint(Color.Blue),
//        )
//    }
}