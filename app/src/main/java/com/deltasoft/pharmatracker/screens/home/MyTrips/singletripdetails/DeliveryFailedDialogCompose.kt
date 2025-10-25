package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.deltasoft.pharmatracker.ui.theme.AppPrimary
import com.deltasoft.pharmatracker.ui.theme.getButtonColors
import com.deltasoft.pharmatracker.ui.theme.getTextButtonColors
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty

@Composable
fun DeliveryFailedConfirmationDialog(showDialog: Boolean,
                                     onConfirm: (comment: String) -> Unit = { a-> },
                                     onDismiss: () -> Unit,
                                     title: String,
                                     message: String,
                                     confirmButtonText: String = "Confirm" ,
                                     dismissButtonText: String = "Cancel",) {
    if (showDialog) {
        // 1. State to hold the user's input
        var commentText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                androidx.compose.material.Text(
                    text = title, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                )
            },
            text = {
//                androidx.compose.material.Text(
//                    text = message,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface
//                )


                // 2. OutlinedTextField with minLines and maxLines set to 4
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Enter your comment") },
                    // KEY PROPERTY: Forces the field to be 4 lines tall visually
                    minLines = 4,
                    // KEY PROPERTY: Prevents the field from growing beyond 4 lines
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth() // Makes the field take up the full width
                        .padding(16.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (commentText.isNotNullOrEmpty()) {
                        onConfirm.invoke(commentText)
                    }
                },
                    enabled = commentText.isNotNullOrEmpty(),
                    colors = getTextButtonColors()
                ) {
                    androidx.compose.material.Text(
                        confirmButtonText,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss,
                    colors = getTextButtonColors()
                ) {
                    androidx.compose.material.Text(
                        dismissButtonText,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}


@Composable
fun DeliveryFailedConfirmationDialogCustom(showDialog: Boolean,
                                     onConfirm: (comment: String) -> Unit = { a-> },
                                     onDismiss: () -> Unit,
                                     title: String,
                                     message: String,
                                     confirmButtonText: String = "Submit" ,
                                     dismissButtonText: String = "Cancel",) {
    var commentText by remember { mutableStateOf("") }
    if (showDialog) {
        Dialog(
            onDismissRequest = {
                commentText = ""
                onDismiss.invoke()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false) ) {
            // The Box will fill the entire space allocated to the Dialog's content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)) {
                    Column(Modifier
                        .fillMaxWidth()
                        .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = title, style = MaterialTheme.typography.bodyLarge,
                            color = AppPrimary, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            label = { Text("Describe issue") },
                            // KEY PROPERTY: Forces the field to be 4 lines tall visually
                            minLines = 4,
                            // KEY PROPERTY: Prevents the field from growing beyond 4 lines
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth() // Makes the field take up the full width
                                .padding(16.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(onClick = {
                                commentText = ""
                                onDismiss.invoke()
                            },
                                colors = getButtonColors()
                            ) {
                                Text(
                                    dismissButtonText
                                )
                            }
                            Button(onClick = {
                                if (commentText.isNotNullOrEmpty()) {
                                    onConfirm.invoke(commentText)
                                    commentText = ""
                                }
                            },
                                enabled = commentText.isNotNullOrEmpty(),
                                colors = getButtonColors()
                            ) {
                                Text(
                                    confirmButtonText
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}