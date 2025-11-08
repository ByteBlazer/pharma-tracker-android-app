package com.deltasoft.pharmatracker.screens.home.scan

import android.content.Context
import android.util.Log
import android.util.Rational
import android.util.Size
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.deltasoft.pharmatracker.utils.AppConstants
import com.deltasoft.pharmatracker.utils.AppUtils
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors


// Composable function for the camera preview and analysis.
@Composable
fun CameraPreviewNew(onBarcodeScanned: (String) -> Unit, requiredConsecutiveScans: Int = AppConstants.REQUIRED_CONSECUTIVE_SCANS, zoomRatio: Float = AppConstants.ZOOM_RATIO) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val size = 1080

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
                val preview = Preview.Builder()
                    .setTargetResolution(Size(size, size))
                    .build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }

//                    Preview.Builder().build().also {
//                    it.setSurfaceProvider(previewView.surfaceProvider)
//                }

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
//                    .setResolutionSelector(resolutionSelector)
                    .setTargetResolution(Size(size, size))
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

                // *** NEW: Create ViewPort for a square region ***
                previewView.post { // Ensures dimensions are ready
                    val width =
                        previewView.width // generally matches parent Modifier due to aspectRatio(1f)
                    val height = previewView.height

                    val viewPort = ViewPort.Builder(
                        Rational(previewView.width, previewView.height), previewView.display.rotation
                    ).build()

//                        ViewPort.Builder(
//                        Rational(width, height), // 1:1 aspect ratio
//                        previewView.display.rotation
//                    ).build()

                    // Build UseCaseGroup with both preview and analysis
                    val useCaseGroup = UseCaseGroup.Builder()
                        .addUseCase(preview)
                        .addUseCase(imageAnalysis)
                        .setViewPort(viewPort)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            useCaseGroup
                        )
                        setCameraZoomNew(camera, zoomRatio, ctx)
                    } catch (e: Exception) {
                        Log.e("BarcodeScanner", "Camera binding failed", e)
                    }
                }

//                try {
//                    // Unbind any previous use cases.
//                    cameraProvider.unbindAll()
//
//                    // Bind the preview and image analysis use cases to the camera.
//                    val camera = cameraProvider.bindToLifecycle(
//                        lifecycleOwner,
//                        cameraSelector,
//                        preview,
//                        imageAnalysis
//                    )
//                    // 4. SAFELY SET ZOOM RATIO
//                    setCameraZoom(camera, zoomRatio, context)
//                } catch (e: Exception) {
//                    Log.e("BarcodeScanner", "Camera binding failed", e)
//                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

// Helper function to safely set the zoom ratio
fun setCameraZoomNew(camera: Camera, desiredRatio: Float, context: Context) {
    val zoomState = camera.cameraInfo.zoomState.value
    if (zoomState != null) {
        val minRatio = zoomState.minZoomRatio
        val maxRatio = zoomState.maxZoomRatio
        // Clamp the desired ratio to be within the valid range
        val clampedRatio = desiredRatio.coerceIn(minRatio, maxRatio)
        camera.cameraControl.setZoomRatio(clampedRatio)
    }
}