package com.deltasoft.pharmatracker.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.*
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun App_AlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String ="",
    dialogText: String ="",
    confirmButtonText : String="",
//    dismissButtonText : String,
//    confirmButtonColor: Color = ButtonDefaults.buttonColors(),
//    dismissButtonColor: Color = AppTheme.colors.txtPrimary
//    icon: ImageVector,
) {
    AlertDialog(
//        icon = {
//            Icon(icon, contentDescription = "Example Icon")
//        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(confirmButtonText)
            }
        },
//        dismissButton = {
//            TextButton(
//                onClick = {
//                    onDismissRequest()
//                }
//            ) {
//                Text(dismissButtonText)
//            }
//        },
//        containerColor = AppTheme.colors.bgSecondary
    )
}