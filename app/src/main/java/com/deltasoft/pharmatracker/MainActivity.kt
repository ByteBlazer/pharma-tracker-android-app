package com.deltasoft.pharmatracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.deltasoft.pharmatracker.navigation.AppNavigation
import com.deltasoft.pharmatracker.ui.theme.PharmaTrackerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        enableEdgeToEdge()
        setContent {
            PharmaTrackerAppTheme {
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

//        val list = AppSignatureHashHelper(applicationContext).appSignatures
//
//        Log.d("TAG", "AppSignatureHelper "+list)
//
//        val appSignatureHelper = AppSignatureHelper(this)
//        val appCodes = appSignatureHelper.getAppSignatures()
//        Log.d("AppHash", "AppSignatureHelper New: $appCodes")
//
//        // Use the AppSignatureHelper to get the hash
//        val appSignatures = AppSignatureHelper(this).getAppSignatures()
//
//        // Log the hash to your console
//        if (appSignatures.isNotEmpty()) {
//            val hash = appSignatures[0]
//            Log.d("AppSignature", "AppSignatureHelper SMS Retriever Hash: ${hash.uppercase(Locale.getDefault())}")
//        }

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
