package com.deltasoft.pharmatracker.screens.otp


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.navigation.Screen


@Composable
fun OtpVerificationScreen(
    navController: NavHostController,
    phoneNumber: String,
    otpVerificationViewModel: OtpVerificationViewModel = viewModel()
) {
    var otp by remember { mutableStateOf("") }
    val otpVerificationState by otpVerificationViewModel.otpVerificationState.collectAsState()

    LaunchedEffect(otpVerificationState) {
        if (otpVerificationState is OtpVerificationState.Success) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) {
                    inclusive = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Otp verification Page", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it },
            label = { Text("OTP") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                    Text(text = "Log In")
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