package com.deltasoft.pharmatracker.screens.otp


import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.navigation.Screen
import com.deltasoft.pharmatracker.screens.login.OTPTextField
import com.deltasoft.pharmatracker.screens.login.OtpTextFieldDefaults
import kotlinx.coroutines.delay


@Composable
fun OtpVerificationScreen(
    navController: NavHostController,
    phoneNumber: String,
    otpVerificationViewModel: OtpVerificationViewModel = viewModel()
) {
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

    LaunchedEffect(otpVerificationState) {
        if (otpVerificationState is OtpVerificationState.Success) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) {
                    inclusive = true
                }
            }
        }
    }
    val annotatedMessageString = buildAnnotatedString {
        append("Enter the OTP sent to  ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append("+91-"+phoneNumber)
        }
        append(" on this mobile number")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "OTP verification", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = annotatedMessageString, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
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
                    enabled = false
                ) {
                    Text("Resend OTP in $timeLeft s",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Don't receive the OTP?", style = MaterialTheme.typography.bodyLarge)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(text = "RESEND OTP", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    TextButton(
                        onClick = {
                            // Reset the timer and run the resend action
                            timeLeft = 30
                            isTimerRunning = true
                        otpVerificationViewModel.onResendClick(phoneNumber)
                        },
                    ) {
                        Text("Resend OTP",
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
                        otpVerificationViewModel.verifyOtp(phoneNumber,otp)
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