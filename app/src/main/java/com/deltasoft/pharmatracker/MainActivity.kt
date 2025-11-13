package com.deltasoft.pharmatracker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
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
import androidx.activity.viewModels

import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.android.play.core.ktx.startUpdateFlowForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.deltasoft.pharmatracker.navigation.Screen
import com.deltasoft.pharmatracker.screens.home.trips.ScheduledTripsState
import com.deltasoft.pharmatracker.utils.AppUtils
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"
class MainActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager

    private val viewModel: MainActivityViewModel by viewModels()

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
                    AppNavigation(applicationContext = applicationContext, mainActivityViewModel = viewModel)
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

//        val appSignature = AppSignatureHashHelper(applicationContext).appSignatures.firstOrNull()
//        Log.d(TAG, "App Signature: $appSignature")

        listenViewModel()

//        if (isIgnoringBatteryOptimizations()) {
//        } else {
//            // Critical step for continuous tracking
//            showBatteryOptimizationDialog()
//        }
    }

    private fun listenViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // --- 1. Launch a coroutine for the first flow (Last Login Time) ---
                launch {
                    viewModel.lastLogInTimeInMills.collect { timeInMills ->
                        if (timeInMills != null) {
                            val currentTime = System.currentTimeMillis()
                            val timeDifference = currentTime - timeInMills
                            val isRecent = timeDifference <= 5 * 1000L

                            if (isRecent) {
                                Log.d("VMListener", "✅ Login is recent (within 5s).")
                                if (isFineLocationPermissionGranted(this@MainActivity)) {
                                    Log.d("VMListener", "✅ Location permission given")
                                    viewModel.getMyTripsList()
                                }else{
                                    Log.d("VMListener", "❌ Location permission not given")
                                }
                            } else {
                                Log.d("VMListener", "❌ Login is old (Diff: $timeDifference ms).")
                            }
                        } else {
                            Log.d("VMListener", "Login time is null.")
                        }
                    }
                }

                // --- 2. Launch a coroutine for the second flow (Scheduled Trips State) ---
                launch {
                    viewModel.scheduledTripsState.collect { state ->
                        when (state) {
                            is ScheduledTripsState.Idle -> {
                                Log.i("VMListener", "Trips State: Idle.")
                            }
                            is ScheduledTripsState.Loading -> {
                                Log.i("VMListener", "Trips State: Loading...")
                                // UI: Show progress spinner
                            }
                            is ScheduledTripsState.Success -> {
                                val scheduledTripsResponse =
                                    (state as ScheduledTripsState.Success).scheduledTripsResponse
                                val anyTripIsCurrentlyActive =
                                    scheduledTripsResponse?.trips?.any { it?.status.equals("STARTED") }?:false
                                Log.d("VMListener", "anyTripIsCurrentlyActive $anyTripIsCurrentlyActive")
                                if (anyTripIsCurrentlyActive){
                                    AppUtils.restartForegroundService(applicationContext)
                                }else{
                                    AppUtils.stopService(applicationContext)
                                }
                            }
                            is ScheduledTripsState.Error -> {
                                val message = (state as ScheduledTripsState.Error).message
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                                viewModel.clearState()

                                Log.d("VMListener", "apiRetryAttempt "+viewModel.apiRetryAttempt)
                                if (viewModel.apiRetryAttempt <= 5) {
                                    viewModel.apiRetryAttempt += 1
                                    viewModel.getMyTripsList(delay = 1000)
                                }
                            }
                        }
                    }
                }
                launch {
                    viewModel.checkBatteryOptimizationClickEvent.collect { timeInMills ->
                        Log.d(TAG, "listenViewModel: timeInMills1 "+timeInMills)
                        if (timeInMills != null) {
                            Log.d(TAG, "listenViewModel: isIgnoringBatteryOptimizations() "+isIgnoringBatteryOptimizations())
                            if (isIgnoringBatteryOptimizations()) {
                            } else {
                                // Critical step for continuous tracking
                                showBatteryOptimizationDialog()
                            }
                        } else {
                            Log.d("VMListener", "Login time is null.")
                        }
                    }
                }
            }
        }
    }

    fun isFineLocationPermissionGranted(activity: MainActivity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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

    private fun isIgnoringBatteryOptimizations(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            return pm.isIgnoringBatteryOptimizations(packageName)
        }
        return true
    }

    private fun showBatteryOptimizationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Critical: Continuous Tracking")
            .setMessage("To ensure reliable location tracking while the app is in the background, please exempt this app from Android's battery restrictions.")
            .setPositiveButton("Go to Settings") { _, _ ->
                requestIgnoreBatteryOptimizations()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Cannot go online without optimization exemption.", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
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
