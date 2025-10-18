package com.deltasoft.pharmatracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.deltasoft.pharmatracker.navigation.AppNavigation
import com.deltasoft.pharmatracker.ui.theme.PharmaTrackerAppTheme
import com.deltasoft.pharmatracker.utils.createappsignature.AppSignatureHashHelper
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import androidx.activity.result.ActivityResult

import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.startUpdateFlowForResult
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "MainActivity"
class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                // Update accepted, app will restart automatically after install
            }
            RESULT_CANCELED -> {
                // User canceled the update — handle it accordingly
                Toast.makeText(this, "Update canceled. The app will close.", Toast.LENGTH_LONG).show()
                finishAffinity() // Optionally close the app
            }
            else -> {
                // Update process failed — handle retry or exit
                Toast.makeText(this, "Update failed. Please try again later.", Toast.LENGTH_LONG).show()
                retryInAppUpdate() // Optionally retry
            }
        }
    }

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

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdate()
    }

    private fun retryInAppUpdate() {
        Handler(Looper.getMainLooper()).postDelayed({
            checkForAppUpdate()
        }, 2000) // Retry after 5 seconds
    }

    private fun checkForAppUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }


    override fun onResume() {
        super.onResume()
        // Resume if update is already in progress
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }
}
