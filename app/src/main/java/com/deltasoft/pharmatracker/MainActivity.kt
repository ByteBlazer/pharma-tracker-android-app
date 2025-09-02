package com.deltasoft.pharmatracker

import android.os.Bundle
import androidx.activity.ComponentActivity
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
    }
}
