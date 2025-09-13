package com.deltasoft.pharmatracker.screens


import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.navigation.Screen
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import com.deltasoft.pharmatracker.utils.jwtdecode.JwtDecodeUtil
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController, context: Context) {
    val sharedPrefsUtil = SharedPreferencesUtil(context)
    val token = sharedPrefsUtil.getString(PrefsKey.USER_ACCESS_TOKEN)
    val phoneNumber = sharedPrefsUtil.getString(PrefsKey.PHONE_NUMBER)


    LaunchedEffect(key1 = true) {
        delay(1000)
        if (AppUtils.isValidToken(token)){
            AppUtils.storePayLoadDetailsToSharedPreferences(sharedPrefsUtil,token)
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) {
                    inclusive = true
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