package com.deltasoft.pharmatracker.screens.home.profile

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.navigation.Screen
import com.deltasoft.pharmatracker.screens.App_CommonTopBar
import com.deltasoft.pharmatracker.screens.otp.OtpVerificationState
import com.deltasoft.pharmatracker.ui.theme.getButtonColors
import com.deltasoft.pharmatracker.ui.theme.getTextButtonColors
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil

private const val TAG = "ProfileScreen"
@Composable
fun ProfileScreen(
    navController: NavHostController, context: Context) {

    val sharedPrefsUtil = SharedPreferencesUtil(context)
    val userName = sharedPrefsUtil.getString(PrefsKey.USER_NAME)
    val userId = sharedPrefsUtil.getString(PrefsKey.USER_ID)
    val phone = sharedPrefsUtil.getString(PrefsKey.PHONE_NUMBER)

    var showDialog by remember { mutableStateOf(false) }
    LogoutConfirmationDialog(
        showDialog = showDialog,
        onConfirm = {
            AppUtils.stopService(context)
            sharedPrefsUtil.saveString(PrefsKey.USER_ACCESS_TOKEN,"")
            navController.navigate(Screen.Login.createRoute(phone)) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
            showDialog = false // Hide the dialog after action
        },
        onDismiss = {
            showDialog = false
        }
    )

    Scaffold(
        topBar = {
            App_CommonTopBar(title = "Profile", onBackClick = {  navController.popBackStack() })
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    Modifier.fillMaxWidth(fraction = 0.75f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        imageVector = Icons.Default.Person,
                        contentDescription = "",
                        modifier = Modifier.size(96.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                    Spacer(Modifier.height(16.dp))
                    SingleRowItem("User ID", userId)
                    SingleRowItem("User name", userName)
                    SingleRowItem("Phone", phone)

                    Button(
                        onClick = {
                            showDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = getButtonColors()
                    ) {
                        androidx.compose.material3.Text(text = "LOGOUT")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SingleRowItem(key: String, value: String?) {
    if (value.isNotNullOrEmpty()) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            text = {
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        text = key ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = value ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        )
    }
}

@Composable
private fun LogoutConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = "Confirm Logout", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "Are you sure you want to log out?", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)
            },
            confirmButton = {
                TextButton(onClick = onConfirm,
                    colors = getTextButtonColors()
                ) {
                    Text("Confirm",
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss,
                    colors = getTextButtonColors()
                ) {
                    Text("Cancel",
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}