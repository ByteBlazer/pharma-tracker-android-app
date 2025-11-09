package com.deltasoft.pharmatracker.screens.home.scan

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.screens.BorderSide
import com.deltasoft.pharmatracker.screens.ScanUnscanSegmentedControl
import com.deltasoft.pharmatracker.screens.drawOneSideBorder
import com.deltasoft.pharmatracker.ui.theme.getButtonColors
import com.deltasoft.pharmatracker.ui.theme.getIconButtonColors
import com.deltasoft.pharmatracker.utils.AppConstants
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
    var isCameraPermissionClicked by remember { mutableStateOf(false) }
    var buttonText by remember { mutableStateOf("Press start to scan") }


    var isScanning by remember { mutableStateOf(false) }

    var scannedValue by remember { mutableStateOf("") }

    var lastApiCalledValue by remember { mutableStateOf<String?>(null) }

    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }
    val dialogMessageColor = remember { mutableStateOf(Color.Green) }

    var scanMode by remember { mutableStateOf(true) }

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
                scanMode = true
                val message = (scanState as ScanDocState.Success).message
                val code = (scanState as ScanDocState.Success).code
                dialogMessage.value = message
                dialogMessageColor.value = getColorFromCode(code)
                showDialog.value = true
                AppVibratorManager.vibrate(context)
                AppUtils.playMediaSound(context,R.raw.positive_beep_1)

                delay(2000L)
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
                AppUtils.playMediaSound(context,R.raw.negative_beep)

                delay(3000L)
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

    // NEW LaunchedEffect to react to permission status changes
    LaunchedEffect(cameraPermissionState.status) {
        if (cameraPermissionState.status.isGranted && isCameraPermissionClicked) {
            Log.d(TAG, "Camera permission GRANTED.")
            isScanning = true
            showDialog.value = false
            isCameraPermissionClicked = false
        } else {
            Log.d(TAG, "Camera permission DENIED or not yet requested.")
        }
    }

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
                scanViewModel.scanDoc(scannedValue,!scanMode)
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

    val redGradient = Brush.verticalGradient(
        colors = listOf(Color.Red, Color(0xFFFF4081)), // From a bright red to a pinkish red
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ScanUnscanSegmentedControl(
                isScanning = scanMode,
                // 3. Pass the function to update the state
                onToggle = { newState ->
                    scanMode = !scanMode
                }
            )
            Column(Modifier.padding(bottom = 48.dp)) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .then(if (scanMode) Modifier else Modifier
                            .background(brush = redGradient,RoundedCornerShape(12.dp)).padding(8.dp)),
                    elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
                ) {
                    // Only show the camera preview if scanning is active.
                    if (isScanning && scannedValue.isNullOrEmpty()) {
                        CameraPreviewNew(
                            onBarcodeScanned = { value ->
                                Log.d("SREENATH", "BarCodeScanner: $value")
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
                            } else {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))


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
                                    isCameraPermissionClicked = false
                                }

                                // 3. If the user has denied the permission, show a rationale
                                //    or guide them to settings.
                                cameraPermissionState.status.shouldShowRationale -> {
                                    isCameraPermissionClicked = true
                                    cameraPermissionState.launchPermissionRequest()
                                    isPermissionCheckedOnce = true
                                }

                                // 4. If it's the first time or they've denied permanently,
                                //    show a button to request permission.
                                else -> {
                                    isCameraPermissionClicked = true
                                    if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale && isPermissionCheckedOnce) {
                                        AppUtils.openAppSettings(context)
                                    } else {
                                        cameraPermissionState.launchPermissionRequest()
                                        isPermissionCheckedOnce = true
                                    }
                                }
                            }
                        },
                        colors = getButtonColors()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_barcode_scanner),
                            contentDescription = "Start Icon",
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isScanning) "STOP" else "START", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Box(modifier = Modifier
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
            .align(Alignment.TopStart)) {
            Column {
                if (showDialog.value) {
                    Card(
                        modifier = Modifier.padding(0.dp),
                        border = BorderStroke(1.dp, dialogMessageColor.value),
                        shape = CardDefaults.outlinedShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 50.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .drawOneSideBorder(
                                    16.dp,
                                    side = BorderSide.LEFT,
                                    color = dialogMessageColor.value
                                )
                                .padding(16.dp),
                            shape = CardDefaults.outlinedShape,
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(dialogMessage.value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                IconButton(onClick = {
                                    showDialog.value = false
                                }, modifier = Modifier,
                                    colors = getIconButtonColors()
                                ) {
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

// State to manage the consecutive scanning for accuracy
data class ScanState(
    val lastValue: String = "",
    val count: Int = 0
)

// Composable function for the camera preview and analysis.
@Composable
fun CameraPreview(onBarcodeScanned: (String) -> Unit,requiredConsecutiveScans: Int = AppConstants.REQUIRED_CONSECUTIVE_SCANS,zoomRatio: Float = AppConstants.ZOOM_RATIO) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // State to track consecutive successful scans
    val scanState = remember { mutableStateOf(ScanState()) }

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

                val resolutionSelector = ResolutionSelector.Builder()
                    .setAspectRatioStrategy(
                        AspectRatioStrategy(
                            AspectRatio.RATIO_16_9,
                            AspectRatioStrategy.FALLBACK_RULE_AUTO // Allows CameraX to select the best fit
                        )
                    )
                    .build()

                // Image analysis use case.
                val imageAnalysis = ImageAnalysis.Builder()
//                    .setTargetResolution(Size(640, 480))
//                    .setTargetResolution(Size(1280, 720))
//                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .setResolutionSelector(resolutionSelector)
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
                                            val currentValue = barcodes[0].rawValue ?: ""

                                            Log.d("SREENATH", "currentValue: $currentValue")
                                            // 3. IMPLEMENT ACCURACY CHECK (Consecutive Scans)
                                            if (currentValue.isNotBlank() && currentValue == scanState.value.lastValue) {
                                                val newCount = scanState.value.count + 1
                                                scanState.value = scanState.value.copy(count = newCount)

                                                if (newCount >= requiredConsecutiveScans) {
                                                    // Barcode verified, emit the result
                                                    onBarcodeScanned(currentValue)

                                                    // Optional: Reset state to prevent immediate re-triggering
                                                    scanState.value = ScanState()
                                                }
                                            } else {
                                                // New or different barcode detected, reset counter
                                                scanState.value = ScanState(lastValue = currentValue, count = 1)
                                            }
                                        } else {
                                            // No barcode found, reset state
                                            scanState.value = ScanState()
                                        }
                                    }
//                                    .addOnSuccessListener { barcodes ->
//                                        if (barcodes.isNotEmpty()) {
//                                            val value = barcodes[0].rawValue ?: "No value found"
//                                            onBarcodeScanned(value)
////                                            Log.d("BarcodeScanner", "Scanned value: $value")
//                                        }
//                                    }
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
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    // 4. SAFELY SET ZOOM RATIO
                    setCameraZoom(camera, zoomRatio, context)
                } catch (e: Exception) {
                    Log.e("BarcodeScanner", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

// Helper function to safely set the zoom ratio
fun setCameraZoom(camera: Camera, desiredRatio: Float, context: Context) {
    val zoomState = camera.cameraInfo.zoomState.value
    if (zoomState != null) {
        val minRatio = zoomState.minZoomRatio
        val maxRatio = zoomState.maxZoomRatio
        // Clamp the desired ratio to be within the valid range
        val clampedRatio = desiredRatio.coerceIn(minRatio, maxRatio)
        camera.cameraControl.setZoomRatio(clampedRatio)
    }
}



