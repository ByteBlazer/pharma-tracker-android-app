package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails

import DrawingPath
import SignaturePad
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.screens.SingleIconWithTextAnnotatedItem
import com.deltasoft.pharmatracker.screens.home.MyTrips.AppCommonApiState
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.Doc
import com.deltasoft.pharmatracker.ui.theme.AppPrimary
import com.deltasoft.pharmatracker.ui.theme.getButtonColors
import com.deltasoft.pharmatracker.ui.theme.getIconButtonColors
import com.deltasoft.pharmatracker.ui.theme.getTextButtonColors
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import com.deltasoft.pharmatracker.utils.AppUtils.showToastMessage

@Composable
fun DeliverySuccessConfirmationDialog(showDialog: Boolean,
                                      onConfirm: (comment: String,signature:String) -> Unit = { a,b-> },
                                      onDismiss: () -> Unit,
                                      title: String,
                                      message: String,
                                      confirmButtonText: String = "Confirm" ,
                                      dismissButtonText: String = "Cancel",) {
    if (showDialog) {
        // 1. State to hold the user's input
        var commentText by remember { mutableStateOf("") }
        var signatureEncodedString by remember { mutableStateOf<String?>("") }

        // Get context to access file system and resources
        val context = LocalContext.current

        // State to hold all the drawing paths (strokes)
        var paths by remember { mutableStateOf(emptyList<DrawingPath>()) }

        // The currently active path being drawn
        var currentPath by remember { mutableStateOf(Path()) }

        // State to store the actual pixel size of the SignaturePad Composable (Source Size)
        var signaturePadSizePx by remember { mutableStateOf(IntSize(0, 0)) }

        // Style settings for the signature
        val signatureColor = Color.Black
        val signatureStrokeWidth = 4.dp

        // Define the fixed dimensions (in DP) of the drawing area for later bitmap scaling
        val signaturePadHeightDp = 300.dp

        // Target width in pixels for the saved image (fixed high quality width)
        val targetBitmapWidth = 800

        val finalPaths = paths + if (!currentPath.isEmpty) {
            listOf(DrawingPath(currentPath, signatureColor, signatureStrokeWidth))
        } else {
            emptyList()
        }

        // Target height will be calculated dynamically to match the canvas aspect ratio
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                androidx.compose.material.Text(
                    text = title, style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                )
            },
            text = {

                // Target height will be calculated dynamically to match the canvas aspect ratio

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF0F4F8)) // Light background
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign Below",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // The Signature Drawing Area
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(signaturePadHeightDp), // Use the defined height DP
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        SignaturePad(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White) // Signature area background
                                // Capture the size of the Composable in pixels
                                .onGloballyPositioned { layoutCoordinates ->
                                    signaturePadSizePx = layoutCoordinates.size
                                },
                            paths = paths,
                            currentPath = currentPath,
                            onSignatureDrawn = { path, dragEnd ->
                                if (dragEnd) {
                                    // Drag ended, save the current path to the list of finished paths
                                    paths = paths + DrawingPath(path, signatureColor, signatureStrokeWidth)
                                    currentPath = Path() // Start a new current path
                                } else {
                                    // FIX: Create a new Path object by copying the content of the mutated 'path'
                                    // This forces Compose to see a state change (new object reference) and recompose the Canvas.
                                    currentPath = Path().apply { addPath(path) }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.size(32.dp))

                    // Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = {
                                paths = emptyList()
                                currentPath = Path()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = getButtonColors()
                        ) {
                            Text("Clear Signature")
                        }


                    }

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
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Combine all paths for final capture
                        if (finalPaths.isNotEmpty() && signaturePadSizePx.width > 0) {
                            // FIX: Dynamically calculate target height based on measured aspect ratio
                            val ratio =
                                signaturePadSizePx.height.toFloat() / signaturePadSizePx.width.toFloat()
                            val calculatedTargetHeight =
                                (targetBitmapWidth.toFloat() * ratio).toInt()

                            signatureEncodedString = AppUtils.saveSignatureImage(
                                context,
                                finalPaths,
                                targetBitmapWidth,
                                calculatedTargetHeight, // Use calculated height for correct proportion
                                signaturePadSizePx.width,
                                signaturePadSizePx.height
                            )
                            if (commentText.isNotNullOrEmpty() && signatureEncodedString.isNotNullOrEmpty()) {
                                onConfirm.invoke(commentText, signatureEncodedString?:"")
                            }else{

                                Log.w(
                                    "SignaturePad",
                                    "comment or signatureEncodedString is empty"
                                )
                            }
                        } else {
                            Log.w(
                                "SignaturePad",
                                "Cannot save: No signature drawn or size not yet measured."
                            )
                        }
                    },
                    enabled = true,
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
fun DeliverySuccessConfirmationDialogCustom(showDialog: Boolean,
                                      onConfirm: (comment: String,signature:String,isChecked:Boolean,location:Location?) -> Unit = { a,b,c,d-> },
                                      onDismiss: () -> Unit,
                                      title: String,
                                      message: String,
                                      confirmButtonText: String = "Confirm" ,
                                      dismissButtonText: String = "Cancel",
                                            singleTripDetailsViewModel: SingleTripDetailsViewModel,
                                            doc: Doc?) {
    // 1. State to hold the user's input
    var commentText by remember { mutableStateOf("") }
    var signatureEncodedString by remember { mutableStateOf<String?>("") }

    // Get context to access file system and resources
    val context = LocalContext.current

    // State to hold all the drawing paths (strokes)
    var paths by remember { mutableStateOf(emptyList<DrawingPath>()) }

    // The currently active path being drawn
    var currentPath by remember { mutableStateOf(Path()) }

    // State to store the actual pixel size of the SignaturePad Composable (Source Size)
    var signaturePadSizePx by remember { mutableStateOf(IntSize(0, 0)) }

    // Style settings for the signature
    val signatureColor = Color.Black
    val signatureStrokeWidth = 4.dp

    // Define the fixed dimensions (in DP) of the drawing area for later bitmap scaling
    val signaturePadHeightDp = 200.dp

    // Target width in pixels for the saved image (fixed high quality width)
    val targetBitmapWidth = 800

    val finalPaths = paths + if (!currentPath.isEmpty) {
        listOf(DrawingPath(currentPath, signatureColor, signatureStrokeWidth))
    } else {
        emptyList()
    }
    var isChecked by remember { mutableStateOf(true) }


    val latestLocationState by singleTripDetailsViewModel.latestLocationState.collectAsState()

    val customerGeoLatitude = doc?.customerGeoLatitude
    val customerGeoLongitude = doc?.customerGeoLongitude
    val isValidCustomerLocationAvailable = customerGeoLatitude.isNotNullOrEmpty() && customerGeoLongitude.isNotNullOrEmpty()

    var isValidLocation by remember { mutableStateOf(!isValidCustomerLocationAvailable) } // if not valid customer location available no location validation needed

    var currentLocation by remember { mutableStateOf<Location?>(null) }

    if (showDialog) {

        // LaunchedEffect triggers whenever clickEvent changes
        LaunchedEffect(Unit) {
            if (isValidCustomerLocationAvailable) {
                singleTripDetailsViewModel.fetchLatestLocation()
            }
        }
        Dialog(
            onDismissRequest = {
                isChecked = true
                commentText = ""
                paths = emptyList()
                currentPath = Path()
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
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = title, style = MaterialTheme.typography.titleLarge,
                            color = AppPrimary, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Customer Signature Below",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // The Signature Drawing Area
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(signaturePadHeightDp), // Use the defined height DP
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                                SignaturePad(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White) // Signature area background
                                        // Capture the size of the Composable in pixels
                                        .onGloballyPositioned { layoutCoordinates ->
                                            signaturePadSizePx = layoutCoordinates.size
                                        },
                                    paths = paths,
                                    currentPath = currentPath,
                                    onSignatureDrawn = { path, dragEnd ->
                                        if (dragEnd) {
                                            // Drag ended, save the current path to the list of finished paths
                                            paths = paths + DrawingPath(path, signatureColor, signatureStrokeWidth)
                                            currentPath = Path() // Start a new current path
                                        } else {
                                            // FIX: Create a new Path object by copying the content of the mutated 'path'
                                            // This forces Compose to see a state change (new object reference) and recompose the Canvas.
                                            currentPath = Path().apply { addPath(path) }
                                        }
                                    }
                                )
                                IconButton(
                                    onClick = {
                                        paths = emptyList()
                                        currentPath = Path()
                                    },
                                    modifier = Modifier,
                                    colors = getIconButtonColors()
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "clear signature",
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }

                        }

//                        Spacer(modifier = Modifier.size(32.dp))

                        // Control Buttons
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceAround
//                        ) {
//                            Button(
//                                onClick = {
//                                    paths = emptyList()
//                                    currentPath = Path()
//                                },
//                                shape = RoundedCornerShape(8.dp),
//                                colors = getButtonColors()
//                            ) {
//                                Text("Clear Signature")
//                            }
//
//
//                        }

                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            label = { Text("Comments (optional)") },
                            // KEY PROPERTY: Forces the field to be 4 lines tall visually
                            minLines = 1,
                            // KEY PROPERTY: Prevents the field from growing beyond 4 lines
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth() // Makes the field take up the full width
                                .padding(top = 16.dp)
                        )



                        Spacer(Modifier.height(16.dp))

                        if (isValidCustomerLocationAvailable) {
                            //Show customer location validation message only when valid customer location available
//                        LaunchedEffect(latestLocationState) {
                            when (latestLocationState) {
                                is LocationState.Idle -> {
                                    Row(Modifier.fillMaxWidth()) {
                                        Text("Fetching location...")
                                        CircularProgressIndicator()
                                    }
                                }

                                is LocationState.Loading -> {
                                    Row(Modifier.fillMaxWidth()) {
                                        Text("Fetching location...")
                                        CircularProgressIndicator()
                                    }
                                }

                                is LocationState.Success -> {
                                    val location1 =
                                        (latestLocationState as LocationState.Success).location
                                    val lat2 = AppUtils.convertLocationStringToDouble(doc?.customerGeoLatitude?:"")
                                    val long2 = AppUtils.convertLocationStringToDouble(doc?.customerGeoLongitude?:"")
                                    val distanceBetweenLocationInMeter = AppUtils.haversineDistanceMeters(
                                        lat1 = location1.latitude,
                                        lon1 = location1.longitude,
                                        lat2 = lat2,
                                        lon2 = long2
                                    )
                                    if (distanceBetweenLocationInMeter<=500) {
                                        currentLocation = location1
                                        isValidLocation = true
                                    }else{

                                        currentLocation = null
                                        isValidLocation = false

                                        val yellowBorder = BorderStroke(
                                            width = 2.dp, // Adjust the thickness of the border line
                                            color = Color.Yellow // Set the color to yellow
                                        )

                                        // Define the shape with rounded corners
                                        val cornerShape = RoundedCornerShape(12.dp)
                                        Row(Modifier.fillMaxWidth()
//                                            .border(
//                                            border = yellowBorder,
//                                            shape = cornerShape)
                                            .background(Color.Yellow.copy(alpha = 0.2f)).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text("You seem to be far from this customer's usual delivery spot", style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f))
//                                            IconButton(
//                                                onClick = {
//                                                   singleTripDetailsViewModel.fetchLatestLocation()
//                                                },
//                                                modifier = Modifier,
//                                                colors = getIconButtonColors()
//                                            ) {
//                                                Icon(
//                                                    painter = painterResource(id = R.drawable.ic_refresh),
//                                                    contentDescription = "retry location",
//                                                    modifier = Modifier.size(24.dp),
//                                                )
//                                            }
                                        }
                                    }
                                }

                                is LocationState.Error -> {
                                    currentLocation = null
                                    isValidLocation = false
                                    val message =
                                        (latestLocationState as LocationState.Error).message

                                    val yellowBorder = BorderStroke(
                                        width = 2.dp, // Adjust the thickness of the border line
                                        color = Color.Yellow // Set the color to yellow
                                    )

                                    // Define the shape with rounded corners
                                    val cornerShape = RoundedCornerShape(12.dp)
                                    Row(Modifier.fillMaxWidth()
//                                            .border(
//                                            border = yellowBorder,
//                                            shape = cornerShape)
                                        .background(Color.Yellow.copy(alpha = 0.2f)).padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("$message Please retry", style = MaterialTheme.typography.bodySmall)
//                                        IconButton(
//                                            onClick = {
//                                                singleTripDetailsViewModel.fetchLatestLocation()
//                                            },
//                                            modifier = Modifier,
//                                            colors = getIconButtonColors()
//                                        ) {
//                                            Icon(
//                                                painter = painterResource(id = R.drawable.ic_refresh),
//                                                contentDescription = "retry location",
//                                                modifier = Modifier.size(24.dp),
//                                            )
//                                        }
                                    }
                                }
                            }
//                        }

                            Spacer(Modifier.height(16.dp))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isChecked = !isChecked } // Toggle state on row click
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { isChecked = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Update customer location", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(onClick = {
                                isChecked = true
                                commentText = ""
                                paths = emptyList()
                                currentPath = Path()
                                onDismiss.invoke()
                            },
                                colors = getButtonColors()
                            ) {
                                Text(
                                    dismissButtonText
                                )
                            }
                            Button(onClick = {
                                // Combine all paths for final capture
                                if (finalPaths.isNotEmpty() && signaturePadSizePx.width > 0) {
                                    // FIX: Dynamically calculate target height based on measured aspect ratio
                                    val ratio =
                                        signaturePadSizePx.height.toFloat() / signaturePadSizePx.width.toFloat()
                                    val calculatedTargetHeight =
                                        (targetBitmapWidth.toFloat() * ratio).toInt()

                                    signatureEncodedString = AppUtils.saveSignatureImage(
                                        context,
                                        finalPaths,
                                        targetBitmapWidth,
                                        calculatedTargetHeight, // Use calculated height for correct proportion
                                        signaturePadSizePx.width,
                                        signaturePadSizePx.height
                                    )
                                    if (signatureEncodedString.isNotNullOrEmpty()) {
                                        onConfirm.invoke(commentText, signatureEncodedString?:"",isChecked,currentLocation)
                                        commentText = ""
                                        paths = emptyList()
                                        currentPath = Path()
                                        isChecked = true
                                    }else{
                                        "Signature is mandatory".showToastMessage(context)
                                        Log.w(
                                            "SignaturePad",
                                            "comment or signatureEncodedString is empty"
                                        )
                                    }
                                } else {
                                    "Signature is mandatory".showToastMessage(context)
                                    Log.w(
                                        "SignaturePad",
                                        "Cannot save: No signature drawn or size not yet measured."
                                    )
                                }
                            },
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