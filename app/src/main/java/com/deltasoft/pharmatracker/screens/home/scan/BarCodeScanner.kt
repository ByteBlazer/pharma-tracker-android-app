package com.deltasoft.pharmatracker.screens.home.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.navigation.Screen
import com.deltasoft.pharmatracker.screens.BorderSide
import com.deltasoft.pharmatracker.screens.drawOneSideBorder
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import com.deltasoft.pharmatracker.utils.AppVibratorManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors


private const val TAG = "BarCodeScanner"
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BarCodeScanner(scanViewModel: ScanViewModel = viewModel()) {
    val context = LocalContext.current



    var isPermissionCheckedOnce by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("Press start to scan") }


    var isScanning by remember { mutableStateOf(false) }

    var scannedValue by remember { mutableStateOf("") }

    var lastApiCalledValue by remember { mutableStateOf<String?>(null) }

    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }
    val dialogMessageColor = remember { mutableStateOf(Color.Green) }

    LaunchedEffect(Unit) {
        scannedValue =""
        lastApiCalledValue = ""
        showDialog.value = false
        scanViewModel.clearScanDocState()
    }

    val scanState by scanViewModel.scanDocState.collectAsState()

    LaunchedEffect(scanState) {
        when (scanState) {
            is ScanDocState.Idle -> {
                Log.d(TAG, "BarCodeScanner: ScanDocState.Idle")

            }

            is ScanDocState.Loading -> {
                Log.d(TAG, "BarCodeScanner: ScanDocState.Loading")
                showDialog.value = false
            }

            is ScanDocState.Success -> {
                Log.d(TAG, "BarCodeScanner: ScanDocState.Success")
                val message = (scanState as ScanDocState.Success).message
                val code = (scanState as ScanDocState.Success).code
                dialogMessage.value = message
                dialogMessageColor.value = getColorFromCode(code)
                showDialog.value = true
                AppVibratorManager.vibrate(context)
                AppUtils.playBeep(100)

                delay(2000L)
                // Hide the dialog after the delay
//                showDialog.value = false
                delay(500L)
                scannedValue = ""
                lastApiCalledValue = ""
            }

            is ScanDocState.Error -> {
                Log.d(TAG, "BarCodeScanner: ScanDocState.Error")
                val message = (scanState as ScanDocState.Error).message
                val code = (scanState as ScanDocState.Error).code
                dialogMessage.value = message
                dialogMessageColor.value = getColorFromCode(code)
                showDialog.value = true
                AppVibratorManager.vibrate(context,500L)
                AppVibratorManager.vibrate(context)
                AppUtils.playBeep(500)

                delay(3000L)
                // Hide the dialog after the delay
//                showDialog.value = false
                delay(500L)
                scannedValue = ""
                lastApiCalledValue = ""
            }
        }
    }

//    if (showDialog.value) {
//        // The actual dialog composable
//        BasicAlertDialog(
//            onDismissRequest = {},
//            modifier = Modifier,
//            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false,usePlatformDefaultWidth = true),
//            content = {
//                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
//                    Card(modifier = Modifier.fillMaxWidth()) {
//                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
//                            Text(text = dialogMessage.value,style = MaterialTheme.typography.bodyLarge, color = dialogMessageColor.value)
//                        }
//                    }
//                }
//            }
//        )
//    }

    // Request camera permission using a launcher.
//    val launcher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted: Boolean ->
//        if (isGranted) {
//            isScanning = true
//            scannedValue = "Scanning..."
//        } else {
//            scannedValue = "Camera permission is required."
//        }
//    }

    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    // Check the permission status
    val allPermissionsGranted = cameraPermissionState.status.isGranted
    val shouldShowRationale = cameraPermissionState.status.shouldShowRationale

    when {
        //If the permission is granted, show the camera preview or button
        cameraPermissionState.status.isGranted -> {
            if (isScanning){
                buttonText = "Stop Scan"
            }else {
                buttonText = "Start Scan"
            }
        }

        // If the user has denied the permission, show a rationale
        cameraPermissionState.status.shouldShowRationale -> {
            buttonText = "Request Permission"
        }

        //  If it's the first time or they've denied permanently, show a button to request permission. or goto settings
        else -> {
            if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale && isPermissionCheckedOnce) {
                buttonText = "Open Settings to Enable Permission"
            } else {
                buttonText = "Request Permission"
            }
        }
    }

    Log.d(TAG, "BarCodeScanner: allPermissionsGranted "+allPermissionsGranted)
    Log.d(TAG, "BarCodeScanner: shouldShowRationale "+shouldShowRationale)

    val message = if (allPermissionsGranted) {
        if (isScanning) {
            "Scanning..."
        } else {
            "Press start to scan."
        }
    } else if (shouldShowRationale)
        "The app needs this permission to function. Please grant it."
    else {
        "Permission is permanently denied. Go to settings to enable it."
    }

//    LaunchedEffect(cameraPermissionState) {
//        if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale) {
//            cameraPermissionState.launchPermissionRequest()
//        }
//    }

    LaunchedEffect(scannedValue) {

        if (scannedValue.isNotNullOrEmpty()) {
            Log.d(TAG, "BarCodeScanner: value available")
            if (scannedValue != lastApiCalledValue) {
                Log.d(TAG, "BarCodeScanner: new value available, calling API")
                lastApiCalledValue = scannedValue
                scanViewModel.scanDoc(scannedValue)
            } else {
                Log.d(TAG, "BarCodeScanner: same value detected, skipping API call")
            }
        } else {
            Log.d(TAG, "BarCodeScanner: value not available")
        }
    }

    LaunchedEffect(scanState) {

        when (scanState) {
            is ScanDocState.Idle -> {
//                CircularProgressIndicator()
            }

            is ScanDocState.Loading -> {
//                CircularProgressIndicator()
            }

            is ScanDocState.Success -> {
                delay(2000)
                scannedValue = ""
            }

            is ScanDocState.Error -> {
                delay(2000)
                scannedValue = ""
                val message = (scanState as ScanDocState.Error).message
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (showDialog.value) {
                    Card(
                        modifier = Modifier.padding(0.dp),
                        border = BorderStroke(1.dp, dialogMessageColor.value),
                        shape = CardDefaults.outlinedShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Card(
                            modifier = Modifier.drawOneSideBorder(
                                16.dp,
                                side = BorderSide.LEFT,
                                color = dialogMessageColor.value
                            ).padding(16.dp),
                            shape = CardDefaults.outlinedShape,
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(dialogMessage.value, modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(4.dp))
                                IconButton(onClick = {
                                    showDialog.value = false
                                }, modifier = Modifier) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "close"
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Column(Modifier.padding(bottom = 48.dp)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                // Only show the camera preview if scanning is active.
                if (isScanning && scannedValue.isNullOrEmpty()) {
                    CameraPreview(
                        onBarcodeScanned = { value ->
                            scannedValue = value
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isScanning) {
                            Text(
                                text = "Press start to scan",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }else{
                            CircularProgressIndicator()
                        }
//                        if (showDialog.value){
////                            Text(
////                                text = dialogMessage.value,
////                                style = MaterialTheme.typography.headlineMedium,
////                                color = dialogMessageColor.value,
////                                modifier = Modifier.fillMaxWidth(),
////                                textAlign = TextAlign.Center
////                            )
//
//                            CircularProgressIndicator()
//                        }else {
//                            if (!isScanning) {
//                                Text(
//                                    text = "Press start to scan",
//                                    style = MaterialTheme.typography.headlineMedium,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                )
//                            }else{
//                                CircularProgressIndicator()
//                            }
//                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

//            // Card to display the scanned barcode value.
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(12.dp),
//                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = if (scannedValue.isNotNullOrEmpty()) scannedValue else "No values available",
//                        style = MaterialTheme.typography.headlineSmall,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))

            // Card to display the scanned barcode value.
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(12.dp),
//                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = message,
//                        style = MaterialTheme.typography.headlineSmall,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//
//            Spacer(modifier = Modifier.height(16.dp))


            // Buttons to control scanning.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = {
                        when {
                            // 2. If the permission is granted, show the camera preview or button
                            cameraPermissionState.status.isGranted -> {
                                isScanning = !isScanning
                                showDialog.value = false
                            }

                            // 3. If the user has denied the permission, show a rationale
                            //    or guide them to settings.
                            cameraPermissionState.status.shouldShowRationale -> {
                                cameraPermissionState.launchPermissionRequest()
                                isPermissionCheckedOnce = true
                            }

                            // 4. If it's the first time or they've denied permanently,
                            //    show a button to request permission.
                            else -> {
                                if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale && isPermissionCheckedOnce) {
                                    openAppSettings(context)
                                } else {
                                    cameraPermissionState.launchPermissionRequest()
                                    isPermissionCheckedOnce = true
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_barcode_scanner),
                        contentDescription = "Start Icon"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isScanning) "STOP" else "START", fontWeight = FontWeight.Bold)
                }
//                when {
//                    // 2. If the permission is granted, show the camera preview or button
//                    cameraPermissionState.status.isGranted -> {
//                        Button(
//                            onClick = {
//                                isScanning = true
//                            },
//                            enabled = !isScanning
//                        ) {
//                            Text("Start Scan")
//                        }
//                    }
//
//                    // 3. If the user has denied the permission, show a rationale
//                    //    or guide them to settings.
//                    cameraPermissionState.status.shouldShowRationale -> {
//                        Button(onClick = {
//                            cameraPermissionState.launchPermissionRequest()
//                            isPermissionCheckedOnce = true
//                        }) {
//                            Text("Request Permission")
//                        }
//                    }
//
//                    // 4. If it's the first time or they've denied permanently,
//                    //    show a button to request permission.
//                    else -> {
//                        Button(onClick = {
//                            if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale && isPermissionCheckedOnce) {
//                                openAppSettings(context)
//                            } else {
//                                cameraPermissionState.launchPermissionRequest()
//                                isPermissionCheckedOnce = true
//                            }
//
//                        }) {
//                            if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale && isPermissionCheckedOnce) {
//                                Text("Open Settings to Enable Permission")
//                            } else {
//                                Text("Request Permission")
//                            }
//                        }
//                    }
//                }

//                when {
//                    allPermissionsGranted -> {
////                    Text("Permission granted! You can use the feature.")
//                        Button(
//                            onClick = {
//                                isScanning = true
//                            },
//                            enabled = !isScanning
//                        ) {
//                            Text("Start Scan")
//                        }
//                    }
//
//                    shouldShowRationale -> {
//                        // Show rationale for the second or subsequent denial
////                    Text("The app needs this permission to function. Please grant it.")
//                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
//                            Text("Request Permission")
//                        }
//                    }
//
//                    else -> {
//                        // Permission permanently denied: ask user to go to settings
////                    Text("Permission is permanently denied. Go to settings to enable it.")
//                        Button(onClick = { openAppSettings(context) }) {
//                            Text("Open App Settings")
//                        }
//                    }
//                }
//            Button(
//                onClick = {
//                    when (PackageManager.PERMISSION_GRANTED) {
//                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
//                            isScanning = true
//                            scannedValue = "Scanning..."
//                        }
//                        else -> {
//                            launcher.launch(Manifest.permission.CAMERA)
//                        }
//                    }
//                },
//                enabled = !isScanning
//            ) {
//                Text("Start Scan")
//            }
//                Button(
//                    onClick = {
//                        isScanning = false
//                        scannedValue = ""
//                    },
//                    enabled = isScanning
//                ) {
//                    Text("Stop Scan")
//                }
            }
            }
        }

    }
}

fun getColorFromCode(code: Int): Color {
    when(code){
        200->{
            return Color.Green
        }
        400->{
            return Color.Red
        }
        500->{
            return Color.Red
        }
        409->{
            return Color.Yellow
        }
        else->{
            return Color.Yellow
        }
    }

}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}


// Composable function for the camera preview and analysis.
@Composable
fun CameraPreview(onBarcodeScanned: (String) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // Embed the Android PreviewView into the Composable layout.
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Configure barcode scanner options.
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_ALL_FORMATS
                    )
                    .build()

                val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(options)
                val analysisExecutor = Executors.newSingleThreadExecutor()

                // Image analysis use case.
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(analysisExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                                // Process the image for barcodes.
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        if (barcodes.isNotEmpty()) {
                                            val value = barcodes[0].rawValue ?: "No value found"
                                            onBarcodeScanned(value)
//                                            Log.d("BarcodeScanner", "Scanned value: $value")
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("BarcodeScanner", "Barcode scanning failed", e)
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            }
                        }
                    }

                // Select the back camera.
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind any previous use cases.
                    cameraProvider.unbindAll()

                    // Bind the preview and image analysis use cases to the camera.
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("BarcodeScanner", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}



