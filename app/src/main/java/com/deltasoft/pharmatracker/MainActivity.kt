package com.deltasoft.pharmatracker

import android.content.Intent
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
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.startUpdateFlowForResult

private const val TAG = "MainActivity"
class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private val appUpdateType = AppUpdateType.IMMEDIATE

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

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        checkAppUpdate()
    }

    override fun onResume() {
        super.onResume()
        if (appUpdateType == AppUpdateType.IMMEDIATE) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        appUpdateType,
                        this,
                        123
                    )
                }
            }
        }
    }
    private fun checkAppUpdate(){
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when(appUpdateType){
                AppUpdateType.IMMEDIATE -> appUpdateInfo.isImmediateUpdateAllowed
                AppUpdateType.FLEXIBLE -> appUpdateInfo.isFlexibleUpdateAllowed
                else -> false
            }
            if (isUpdateAvailable && isUpdateAllowed){
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    appUpdateType,
                    this,
                    123
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            if (resultCode != RESULT_OK) {
                checkAppUpdate()
            }
        }
    }
}
