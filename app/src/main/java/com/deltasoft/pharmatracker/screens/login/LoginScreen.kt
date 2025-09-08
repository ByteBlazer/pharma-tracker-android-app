package com.deltasoft.pharmatracker.screens.login

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.navigation.Screen
import com.deltasoft.pharmatracker.screens.App_CommonTopBar
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty


@Composable
fun LoginScreen(
    navController: NavHostController,
    prefillPhoneNumber:String="",
    loginViewModel: LoginViewModel = viewModel()
) {
    var phoneNumber by remember { mutableStateOf(prefillPhoneNumber) }
    val loginState by loginViewModel.loginState.collectAsState()

    var isNumberValid by remember { mutableStateOf(true) }

    fun validateNumber(number: String) {
//        val mobileNumberPattern = "^[6-9][0-9]{9}$"
//        isNumberValid = number.matches(mobileNumberPattern.toRegex()) || number.isEmpty()
        isNumberValid = number.isNotNullOrEmpty() && (number.length == 10)
    }

    val annotatedMessageString = buildAnnotatedString {
        append(stringResource(R.string.login_description_message_part1)+" ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(stringResource(R.string.login_description_message_part2_bold))
        }
        append(" "+stringResource(R.string.login_description_message_part3))
    }
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            loginViewModel.clearLoginState()
            navController.navigate(Screen.OtpVerification.createRoute(phoneNumber))
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        topBar = {
            App_CommonTopBar(backButtonVisibility = false)
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

            App_CommonTopBar(backButtonVisibility = false)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
//            .imePadding()
                    .padding(16.dp)
                ,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.login_heading), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(32.dp))

                Text(text = annotatedMessageString, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { newText ->
//                phoneNumber = it
                        // Only allow digits to be entered
                        if (newText.length <= 10 && newText.all { it.isDigit() }) {
                            phoneNumber = newText
                        }
                        // Validate the number as the user types
                        validateNumber(phoneNumber)
                        loginViewModel.clearLoginState()
                    },
                    label = { Text(stringResource(R.string.login_text_field_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 1,
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
//                    Icon(
//                        imageVector = Icons.Default.Phone,
//                        contentDescription = "Phone Icon",
//                        modifier = Modifier.padding(start = 16.dp, end = 4.dp)
//                    )
                            Text(
                                text = "+91-",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
//            supportingText = {
//                if (!isNumberValid) {
//                    Text(
//                        modifier = Modifier.fillMaxWidth(),
//                        text = "Invalid mobile number",
//                    )
//                }
//            },
//            isError = !isNumberValid,
                    enabled = loginState !is LoginState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (loginState) {
                    is LoginState.Loading ->
                        CircularProgressIndicator()
                    else -> {
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                loginViewModel.login(phoneNumber)
                            },
                            enabled = loginState !is LoginState.Loading && isNumberValid && phoneNumber.isNotNullOrEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.login_button_text))
                        }
                    }
                }

                if (loginState is LoginState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = (loginState as LoginState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }


}