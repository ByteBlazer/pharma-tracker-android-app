package com.deltasoft.pharmatracker.screens.login

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.navigation.Screen


@Composable
fun LoginScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel = viewModel()
) {
    var phoneNumber by remember { mutableStateOf("") }
    val loginState by loginViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            navController.navigate(Screen.OtpVerification.createRoute(phoneNumber)) {
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
        Text(text = "Login Page", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (loginState) {
            is LoginState.Loading -> CircularProgressIndicator()
            else -> {
                Button(
                    onClick = {
                        loginViewModel.login(phoneNumber)
                    },
                    enabled = loginState !is LoginState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Log In")
                }
            }
        }

        if (loginState is LoginState.Error) {
            Log.d("TAG", "LoginScreen: "+(loginState as LoginState.Error).message)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (loginState as LoginState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}