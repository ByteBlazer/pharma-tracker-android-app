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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deltasoft.pharmatracker.screens.home.route.DispatchQueueListCompose
import com.deltasoft.pharmatracker.screens.home.route.DispatchQueueState
import com.deltasoft.pharmatracker.screens.home.route.DispatchQueueViewModel
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

    val scanState by scanViewModel.scanDocState.collectAsState()

    var isScanning by remember { mutableStateOf(false) }

    var scannedValue by remember { mutableStateOf("") }

    var lastApiCalledValue by remember { mutableStateOf<String?>(null) }

    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }
    val dialogMessageColor = remember { mutableStateOf(Color.Green) }
    LaunchedEffect(scanState) {
        when (scanState) {
            is ScanDocState.Idle -> {

            }

            is ScanDocState.Loading -> {
            }

            is ScanDocState.Success -> {
                val message = (scanState as ScanDocState.Success).message
                val code = (scanState as ScanDocState.Success).code
                dialogMessage.value = message
                dialogMessageColor.value = getColorFromCode(code)
                showDialog.value = true
                AppVibratorManager.vibrate(context)
                AppUtils.playBeep(100)

                delay(3000L)
                // Hide the dialog after the delay
                showDialog.value = false
                delay(500L)
                scannedValue = ""
                lastApiCalledValue = ""
            }

            is ScanDocState.Error -> {
                val message = (scanState as ScanDocState.Error).message
                val code = (scanState as ScanDocState.Error).code
                dialogMessage.value = message
                dialogMessageColor.value = getColorFromCode(code)
                showDialog.value = true
                AppVibratorManager.vibrate(context,500L)
                AppVibratorManager.vibrate(context)
                AppUtils.playBeep(500)

                delay(2000L)
                // Hide the dialog after the delay
                showDialog.value = false
                delay(500L)
                scannedValue = ""
                lastApiCalledValue = ""
            }
        }
    }

    if (showDialog.value) {
        // The actual dialog composable
        BasicAlertDialog(
            onDismissRequest = {},
            modifier = Modifier,
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false,usePlatformDefaultWidth = true),
            content = {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(text = dialogMessage.value,style = MaterialTheme.typography.bodyLarge, color = dialogMessageColor.value)
                        }
                    }
                }
            }
        )
    }

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

    LaunchedEffect(cameraPermissionState) {
        if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

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
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.weight(1f),
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
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (scannedValue.isNullOrEmpty()) "Camera is off" else "Scan successful",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card to display the scanned barcode value.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (scannedValue.isNotNullOrEmpty()) scannedValue else "No values available",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card to display the scanned barcode value.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))


            // Buttons to control scanning.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                when {
                    allPermissionsGranted -> {
//                    Text("Permission granted! You can use the feature.")
                        Button(
                            onClick = {
                                isScanning = true
                            },
                            enabled = !isScanning
                        ) {
                            Text("Start Scan")
                        }
                    }

                    shouldShowRationale -> {
                        // Show rationale for the second or subsequent denial
//                    Text("The app needs this permission to function. Please grant it.")
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Request Permission")
                        }
                    }

                    else -> {
                        // Permission permanently denied: ask user to go to settings
//                    Text("Permission is permanently denied. Go to settings to enable it.")
                        Button(onClick = { openAppSettings(context) }) {
                            Text("Open App Settings")
                        }
                    }
                }
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
                Button(
                    onClick = {
                        isScanning = false
                        scannedValue = ""
                    },
                    enabled = isScanning
                ) {
                    Text("Stop Scan")
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



