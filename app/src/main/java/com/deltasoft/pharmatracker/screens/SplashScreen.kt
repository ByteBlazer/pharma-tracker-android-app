package com.deltasoft.pharmatracker.screens


import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.navigation.Screen
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.jwtdecode.JwtDecodeUtil
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController, context: Context) {
    val sharedPrefsUtil = SharedPreferencesUtil(context)
    val token = sharedPrefsUtil.getString(PrefsKey.USER_ACCESS_TOKEN)


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
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) {
                    inclusive = true
                }
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Splash Screen", fontSize = 24.sp)
    }
}