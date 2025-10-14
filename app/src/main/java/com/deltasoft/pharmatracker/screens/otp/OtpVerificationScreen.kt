package com.deltasoft.pharmatracker.screens.otp


import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.navigation.Screen
import com.deltasoft.pharmatracker.screens.App_CommonTopBar
import com.deltasoft.pharmatracker.screens.home.schedule.ScheduledTripsState
import com.deltasoft.pharmatracker.screens.login.OTPTextField
import com.deltasoft.pharmatracker.screens.login.OtpTextFieldDefaults
import com.deltasoft.pharmatracker.ui.theme.getTextButtonColors
import com.deltasoft.pharmatracker.utils.AppUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.auth.api.phone.SmsRetriever
import kotlinx.coroutines.delay


private const val TAG = "OtpVerificationScreen"
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OtpVerificationScreen(
    navController: NavHostController,
    phoneNumber: String,
    otpVerificationViewModel: OtpVerificationViewModel = viewModel()
) {

    val context = LocalContext.current

    val apiState by otpVerificationViewModel.scheduledTripsState.collectAsState()

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
                    popUpTo(Screen.Login.route) {
                        inclusive = true
                    }
                }
            }
            is ScheduledTripsState.Error -> {
                val message = (apiState as ScheduledTripsState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                otpVerificationViewModel.clearState()

                Log.d(TAG, "SplashScreen: splashViewModel.apiRetryAttempt "+otpVerificationViewModel.apiRetryAttempt)
                if (otpVerificationViewModel.apiRetryAttempt <= 5) {
                    otpVerificationViewModel.apiRetryAttempt += 1
                    otpVerificationViewModel.getMyTripsList(delay = 1000)
                }else{
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    var otp by remember { mutableStateOf("") }
    val otpVerificationState by otpVerificationViewModel.otpVerificationState.collectAsState()

    // State to hold the countdown value
    var timeLeft by remember { mutableStateOf(30) }
    // State to control when the timer should run
    var isTimerRunning by remember { mutableStateOf(true) }

    // Use LaunchedEffect to manage the countdown
    LaunchedEffect(key1 = isTimerRunning) {
        if (isTimerRunning) {
            while (timeLeft > 0) {
                delay(1000) // Delay for 1 second
                timeLeft--
            }
            // Timer has finished, show the resend button
            isTimerRunning = false
        }
    }

    LaunchedEffect(key1 = otp) {
        if (otp.length == 6) {
            otpVerificationViewModel.verifyOtp(phoneNumber, otp)
        }
    }

    LaunchedEffect(otpVerificationState) {
        if (otpVerificationState is OtpVerificationState.Success) {
            if (locationPermissionState.status.isGranted) {
                // If permission granted call api to check any trip is currently active
                otpVerificationViewModel.getMyTripsList()
            } else {
                // permission not granted so directly move to home
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) {
                        inclusive = true
                    }
                }
            }
        }
    }
    val annotatedMessageString = buildAnnotatedString {
        append("Enter the OTP sent to  ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("+91-"+phoneNumber)
        }
    }
    DisposableEffect(context) {

        SmsRetriever.getClient(context).startSmsRetriever()

        val receiver = MySMSBroadcastReceiver().apply {
            initListener(
                object : MySMSBroadcastReceiver.Listener {
                    override fun onOtpReceived(value: String?) {
                        Log.d(TAG, "onOtpReceived: "+value)
                        otp = value?:""
                    }
                }
            )
        }
        val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(receiver,filter, Context.RECEIVER_EXPORTED)
        }

        onDispose {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.unregisterReceiver(receiver)
            }
        }
    }

    Scaffold(
        topBar = {
            App_CommonTopBar(onBackClick = {  navController.popBackStack() })
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            App_CommonTopBar(backButtonVisibility = false, useDefaultColor = true)
            Text(
                text = "OTP verification",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Enter the OTP sent to",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "+91-" + phoneNumber,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

//        OutlinedTextField(
//            value = otp,
//            onValueChange = { otp = it },
//            label = { Text("OTP") },
//            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )


            OTPTextField(
                value = otp, // Initial value
                onTextChanged = { otp = it },
                numDigits = 6, // Number of digits in OTP
                isMasked = false, // Mask digits for security
                digitContainerStyle = OtpTextFieldDefaults.outlinedContainer(), // Choose style (outlined or underlined)
                textStyle = MaterialTheme.typography.titleLarge, // Configure text style
                isError = false // Indicate whether the OTP field is in an error state
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Crossfade to smoothly transition between the timer and the button
            Crossfade(targetState = isTimerRunning, label = "") { running ->
                if (running) {
//                Text(
//                    text = "Resend OTP in $timeLeft s",
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    style = MaterialTheme.typography.bodyLarge
//                )
                    TextButton(
                        onClick = {
                            // Reset the timer and run the resend action
                            timeLeft = 30
                            isTimerRunning = true
//                        onResendClick()
                        },
                        enabled = false,
                        colors = getTextButtonColors()
                    ) {
                        Text(
                            "Resend OTP in $timeLeft s",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't receive the OTP?",
                            style = MaterialTheme.typography.bodyLarge
                        )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(text = "RESEND OTP", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = {
                                // Reset the timer and run the resend action
                                timeLeft = 30
                                isTimerRunning = true
                                otpVerificationViewModel.onResendClick(phoneNumber)
                            },
                            colors = getTextButtonColors()
                        ) {
                            Text(
                                "Resend OTP",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

//        OtpTextField(
//            otpText = otp,
//            onOtpTextChange = { otpValue ->
//                otp = otpValue
//                // You can add validation logic here when the length is 6
//                if (otp.length == 6) {
//                    // Do something with the complete OTP
//                }
//            }
//        )


            when (otpVerificationState) {
                is OtpVerificationState.Loading -> CircularProgressIndicator()
                else -> {
                    Button(
                        onClick = {
                            otpVerificationViewModel.verifyOtp(phoneNumber, otp)
                        },
                        enabled = otpVerificationState !is OtpVerificationState.Loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "VERIFY & PROCEED")
                    }
                }
            }

            if (otpVerificationState is OtpVerificationState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (otpVerificationState as OtpVerificationState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpTextField(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val length = 6
    val focusRequesters = remember { List(length) { FocusRequester() } }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        (0 until length).forEach { index ->
            val digit = if (index < otpText.length) otpText[index].toString() else ""

            OutlinedTextField(
                value = digit,
                onValueChange =  { newValue ->
                    // We're only interested in single-character changes, either adding or deleting.
                    if (newValue.length <= 1) {
                        val newOtpText = otpText.toMutableList()

                        if (newValue.isNotEmpty()) {
                            // A character was added
                            if (index < newOtpText.size) {
                                newOtpText[index] = newValue[0]
                            } else {
                                newOtpText.add(index, newValue[0])
                            }
                            // Move focus to the next box, unless it's the last one
                            if (index < length - 1) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        } else {
                            // A character was deleted
                            // This is the key change: if the current box is not empty, delete the character in it.
                            // If the current box is empty, it means the user backspaced from the next box.
                            if (index < newOtpText.size) {
                                newOtpText.removeAt(index)
                            }

                            // Move focus back to the previous box if it exists
                            if (index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        }
                        onOtpTextChange(newOtpText.joinToString(""))
                    }},
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .focusRequester(focusRequesters[index]),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}