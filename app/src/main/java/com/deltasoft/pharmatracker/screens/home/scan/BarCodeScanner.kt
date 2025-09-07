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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BarCodeScanner() {
    val context = LocalContext.current

    // State to manage whether scanning is active.
    var isScanning by remember { mutableStateOf(false) }

    // State to hold the scanned barcode value.
    var scannedValue by remember { mutableStateOf("No values available") }

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
        if (isScanning){
            "Scanning..."
        }else{
            "Press start to scan."
        }
    } else if (shouldShowRationale)
        "The app needs this permission to function. Please grant it."
    else {
        "Permission is permanently denied. Go to settings to enable it."
    }

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
            if (isScanning) {
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
                        text = "Camera is off",
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
                    text = scannedValue,
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
                    scannedValue = "No values available"
                },
                enabled = isScanning
            ) {
                Text("Stop Scan")
            }
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
                                            Log.d("BarcodeScanner", "Scanned value: $value")
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



