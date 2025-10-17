package com.deltasoft.pharmatracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.deltasoft.pharmatracker.navigation.AppNavigation
import com.deltasoft.pharmatracker.ui.theme.PharmaTrackerAppTheme
import com.deltasoft.pharmatracker.utils.createappsignature.AppSignatureHashHelper

private const val TAG = "MainActivity"
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = AppSignatureHashHelper(this).appSignatures
        Log.d(TAG, "App Hash: $list")
//        enableEdgeToEdge()
        setContent {
            PharmaTrackerAppTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(applicationContext)
                }
            }
        }

        MyApp.logToDataDog("MainActivity has started")
        MyApp.logToDataDog("The API Base URL is: " + BuildConfig.BASE_API_URL)


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Here's your logic to minimize the app
                moveTaskToBack(true)
            }
        }

        // Add the callback to the dispatcher
        onBackPressedDispatcher.addCallback(this, callback)
    }
}
