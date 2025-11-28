package com.deltasoft.pharmatracker.utils

import DrawingPath
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.location.Location
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.deltasoft.pharmatracker.utils.createappsignature.AppSignatureHashHelper
import com.deltasoft.pharmatracker.utils.jwtdecode.JwtDecodeUtil
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.mlkit.vision.common.InputImage
import toAndroidPath
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.*

private const val TAG = "AppUtils"
object AppUtils {
    fun isValidToken(token: String): Boolean {
        if (token.isNotNullOrEmpty()){
            val decodedPayload = JwtDecodeUtil.decodeJwtPayload(token)
            if (decodedPayload != null) {
                Log.d(TAG,"Successfully decoded JWT payload:")
                Log.d(TAG,"ID: ${decodedPayload.id}")
                Log.d(TAG,"Username: ${decodedPayload.username}")
                Log.d(TAG,"Mobile: ${decodedPayload.mobile}")
                Log.d(TAG,"iat: ${decodedPayload.iat}")
                Log.d(TAG,"exp: ${decodedPayload.exp}")

                // Convert the Unix timestamps to a readable Date format
                val issuedAtDate = Date(decodedPayload.iat * 1000)
                val expirationDate = Date(decodedPayload.exp * 1000)

                Log.d(TAG,"Issued At: $issuedAtDate")
                Log.d(TAG,"Expires At: $expirationDate")

                // Get the current time in milliseconds and compare it to the expiration time.
                // The 'exp' field is in seconds, so we multiply by 1000 to convert to milliseconds.
                val currentTimeMillis = System.currentTimeMillis()
                val expirationTimeMillis = decodedPayload.exp * 1000L

                return currentTimeMillis < expirationTimeMillis
            } else {
                Log.d(TAG,"Failed to decode the provided JWT token.")
                return false
            }
        }else{
            //token is null or empty
            Log.d(TAG,"Jwt token is null or empty")
            return false
        }
    }

    fun String?.isNotNullOrEmpty(): Boolean {
        return !this.isNullOrEmpty()
    }

    fun storePayLoadDetailsToSharedPreferences(
        sharedPrefsUtil: SharedPreferencesUtil,
        token: String
    ) {
        val tokenPayload = JwtDecodeUtil.decodeJwtPayload(token?:"")
        sharedPrefsUtil.saveString(PrefsKey.USER_NAME,tokenPayload?.username?:"")
        sharedPrefsUtil.saveString(PrefsKey.USER_ID,tokenPayload?.id?:"")
        sharedPrefsUtil.saveString(PrefsKey.PHONE_NUMBER,tokenPayload?.mobile?:"")
        sharedPrefsUtil.saveString(PrefsKey.ROLES,tokenPayload?.roles?:"")
//        sharedPrefsUtil.saveString(PrefsKey.ROLES,"")
        sharedPrefsUtil.saveInt(PrefsKey.LOCATION_HEART_BEAT_FREQUENCY_IN_SECONDS,tokenPayload?.locationHeartBeatFrequencyInSeconds?:0)
    }

    fun createBearerToken(accessToken: String): String {
        return "Bearer $accessToken"
    }

    fun playBeep(duration: Int) {
        val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        toneGen.startTone(ToneGenerator.TONE_DTMF_S, duration)
        // Release the ToneGenerator to avoid resource leaks
        // You should do this after the tone has finished playing
        // A Handler is a good way to delay the release
        Handler(Looper.getMainLooper()).postDelayed({
            toneGen.release()
        }, (duration + 50).toLong())
    }

    fun playMediaSound(context: Context, resId: Int) {
        // A MediaPlayer is used to control playback of audio files.
        // It's a good practice to declare it as nullable to handle cleanup.
        var mediaPlayer: MediaPlayer? = null

        try {
            // Create a new MediaPlayer instance from the raw resource.
            mediaPlayer = MediaPlayer.create(context, resId)

            // The critical step: setting the audio attributes.
            // For modern Android APIs (21+), we use AudioAttributes to specify the stream type.
            // Using `CONTENT_TYPE_MUSIC` and `USAGE_MEDIA` links this audio to the
            // device's media volume control.
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()

            mediaPlayer?.setAudioAttributes(audioAttributes)

            // For older Android APIs (before 21), you would use this deprecated method:
            // mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            // Start playing the sound.
            mediaPlayer?.start()

            // It is important to release the MediaPlayer resources when the sound has
            // finished playing to prevent memory leaks. We can use a listener for this.
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // In a real application, you would handle this error more gracefully,
            // for example, by showing a Toast message to the user.
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    /**
     * Converts an ISO 8601 UTC timestamp string to a formatted date/time string in IST.
     *
     * @param iso8601Timestamp The timestamp string (e.g., "2025-09-23T19:05:12.451Z").
     * @return The formatted date/time string (e.g., "Sep 24th 12:35 AM") or a default error message.
     */
    fun convertIso8601ToIst(iso8601Timestamp: String): String {
        // Define the desired output format for the date and time
        // 'MMM' for Sep, 'dd' for 24, 'th' is literal, 'hh' for 12-hour, 'mm' for minutes, 'a' for AM/PM
        // Note: The 'th' suffix for '24th' is not automatically generated by standard formatters.
        // For simplicity and correctness with standard formatters, we'll use a standard format
        // and manually construct the day suffix for a specific output as shown in the prompt.

        val istZoneId = ZoneId.of("Asia/Kolkata") // IST zone ID

        try {
            // 1. Parse the ISO 8601 string into an Instant (which is always in UTC)
            val instant = Instant.parse(iso8601Timestamp)

            // 2. Convert the UTC Instant to a ZonedDateTime in the IST timezone
            val zonedDateTime = instant.atZone(istZoneId)

            // 3. Extract day and use a helper to get the ordinal suffix (st, nd, rd, th)
            val dayOfMonth = zonedDateTime.dayOfMonth
            val dayWithSuffix = "$dayOfMonth${getDayOfMonthSuffix(dayOfMonth)}"

            // 4. Define a formatter for the rest of the string
            // 'MMM' for Sep, 'h' for 12-hour (no leading zero), 'mm' for minutes, 'a' for AM/PM
            val timeFormatter = DateTimeFormatter.ofPattern("MMM hh:mm a", Locale.ENGLISH)

            // 5. Format the ZonedDateTime using the timeFormatter
            val formattedTime = timeFormatter.format(zonedDateTime)

            // 6. Replace the day number in the formatted output with the day+suffix
            // The formatter gives "Sep 24 12:35 AM", so we need to inject the suffix.
            // Instead of injecting, let's construct the final string.

            // The requested output format is "Sep 26th 2:23 PM" (Month DaySuffix Time)
            // We need: Month (MMM), DaySuffix (from above), Time (h:mm a)

            val monthString = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH).format(zonedDateTime)
            val timeString = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH).format(zonedDateTime)

            return "$monthString $dayWithSuffix $timeString"

        } catch (e: Exception) {
            // Handle parsing errors, e.g., if the input string is malformed
            e.printStackTrace()
            return "Invalid Date"
        }
    }

    /**
     * Helper function to determine the ordinal suffix (st, nd, rd, th) for a day of the month.
     */
    fun getDayOfMonthSuffix(day: Int): String {
        if (day in 11..13) {
            return "th"
        }
        return when (day % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    /**
     * Utility function to open Google Maps and start navigation to a specified
     * latitude and longitude.
     *
     * This function checks if the Google Maps app is available. If it is, it
     * launches a navigation Intent. If not, it falls back to showing an error.
     *
     * @param context The application or activity context.
     * @param latitude The destination latitude (e.g., 9.9816).
     * @param longitude The destination longitude (e.g., 76.2999).
     * @param destinationName An optional name for the destination (e.g., "Vytilla Office").
     */
    fun startGoogleMapsNavigation(
        context: Context,
        latitude: String,
        longitude: String,
        destinationName: String = "Destination"
    ) {
        Log.d(TAG, "startGoogleMapsNavigation: latitude "+latitude)
        Log.d(TAG, "startGoogleMapsNavigation: longitude "+longitude)
        // 1. Define the URI for navigation (using 'daddr' for destination address)
        // 'q' is used for the query, combining lat/lng and a label.
        // 'mode=d' requests driving directions.
        val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")

//        val gmmIntentUri = Uri.parse("geo:0,0?q=$latitude,$longitude($destinationName)")

        // 2. Create an Intent, specifying the ACTION_VIEW and setting the data URI
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        // 3. Explicitly set the package to ensure only the Google Maps app handles the Intent
        mapIntent.setPackage("com.google.android.apps.maps")

        // 4. Check if there is an app available to handle this Intent
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback: Google Maps app is not installed
            Toast.makeText(
                context,
                "Google Maps app not found. Please install it to use navigation.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun startGoogleMapsDirections(
        context: Context,
        latitude: String,
        longitude: String,
        destinationName: String = "Destination"
    ) {
        Log.d("MapsNavigation", "startGoogleMapsDirections: latitude $latitude")
        Log.d("MapsNavigation", "startGoogleMapsDirections: longitude $longitude")

        // 1. Define the URI to request directions.
        // We use the 'geo' scheme with the 'daddr' parameter (destination address).
        // The format is daddr=latitude,longitude(Label).
        val gmmIntentUri = Uri.parse("geo:0,0?q=$latitude,$longitude($destinationName)")

        // **Alternative URI for Directions** (sometimes preferred):
        // val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&destination_place_id=&travelmode=driving")


        // 2. Create an Intent, specifying the ACTION_VIEW and setting the data URI
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        // 3. Explicitly set the package to ensure only the Google Maps app handles the Intent
        mapIntent.setPackage("com.google.android.apps.maps")

        // 4. Check if there is an app available to handle this Intent
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback: Google Maps app is not installed
            Toast.makeText(
                context,
                "Google Maps app not found. Please install it to use directions.",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    /**
     * Converts the list of Compose Paths into an Android Bitmap and saves it as a PNG file.
     *
     * @param context The Android context for file operations.
     * @param finalPaths The list of all completed signature strokes.
     * @param targetWidth The desired pixel width of the output image.
     * @param targetHeight The desired pixel height of the output image.
     * @param sourceWidthPx The actual pixel width of the composable where the path was drawn (Source).
     * @param sourceHeightPx The actual pixel height of the composable where the path was drawn (Source).
     */
    fun saveSignatureImage(
        context: Context,
        finalPaths: List<DrawingPath>,
        targetWidth: Int,
        targetHeight: Int,
        sourceWidthPx: Int,
        sourceHeightPx: Int
    ): String? {
        if (finalPaths.isEmpty() || sourceWidthPx <= 0 || sourceHeightPx <= 0) return null

        // 1. Create an empty Bitmap
        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 2. Draw a solid white background onto the Bitmap
        canvas.drawColor(android.graphics.Color.WHITE)

        // 3. Define the Paint object for drawing the strokes
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        // Calculate scaling factors
        val scaleX = targetWidth.toFloat() / sourceWidthPx.toFloat()
        val scaleY = targetHeight.toFloat() / sourceHeightPx.toFloat()

        // 4. Draw each Compose Path onto the Android Canvas
        finalPaths.forEach { drawingPath ->

            paint.color = drawingPath.color.toArgb() // Convert Compose Color to Android Color

            // Scale stroke width relative to the scaling factor (using X for both)
            paint.strokeWidth = drawingPath.strokeWidth.value * scaleX

            // Convert the Compose Path to the native Android Path
            val androidPath = drawingPath.path.toAndroidPath()

            // Apply scaling matrix to the Path coordinates
            val matrix = Matrix()
            matrix.setScale(scaleX, scaleY)
            androidPath.transform(matrix) // Apply the transformation

            canvas.drawPath(androidPath, paint)
        }


        // 5. Compression and Iterative Loop for File Size Control
//        val filename = "signature_${System.currentTimeMillis()}.jpeg"
        val filename = "signature.jpeg"
        val file = File(context.filesDir, filename)

        // Target file size limits
        val maxAcceptableBytes = 10 * 1024L // 10 KB
        val minForcedBytes = 7 * 1024L     // 7 KB

        var currentQuality = 100 // Start at 100 as requested
        var fileSizeInBytes: Long
        var attempts = 0
        val maxAttempts = 100 // Safety break

        // Initial Compression at Quality 100
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, out)
            }
            fileSizeInBytes = file.length()
        } catch (e: IOException) {
            Log.e("SignaturePad", "Error during initial saving: ${e.message}")
            return null
        }

        // Iterative Reduction Loop (only if the initial file size is too large)
        if (fileSizeInBytes > maxAcceptableBytes) {

            Log.d("SignaturePad", "Initial size (${fileSizeInBytes / 1024.0} KB) > 10KB. Starting reduction...")

            // Reset quality just below 100 and start iterating down, now targeting 7KB-10KB
            currentQuality = 99

            while ((fileSizeInBytes > maxAcceptableBytes || fileSizeInBytes < minForcedBytes) &&
                currentQuality in 1..99 &&
                attempts < maxAttempts) {

                attempts++

                // Re-compress the bitmap
                try {
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, out)
                    }
                    fileSizeInBytes = file.length()
                } catch (e: IOException) {
                    Log.e("SignaturePad", "Error during iterative compression: ${e.message}")
                    break // Exit loop on IO error
                }


                Log.d("SignaturePad", " attempt  "+attempts)
                Log.d("SignaturePad", " currentQuality  "+currentQuality)
                if (fileSizeInBytes > maxAcceptableBytes) {
                    // File is too large (> 10KB), reduce quality.
                    currentQuality -= if (currentQuality > 80) 5 else 1 // Larger steps for faster convergence
                    if (currentQuality < 1) currentQuality = 1
                } else if (fileSizeInBytes < minForcedBytes) {
                    // File is too small (< 7KB), increase quality slightly to increase size
                    currentQuality += 1
                    if (currentQuality > 100) currentQuality = 100
                } else {
                    // Success: within 7KB-10KB range
                    break
                }
            }
        }


        // Final log message
        val finalFileSizeInKB = fileSizeInBytes / 1024.0
        val status = if (fileSizeInBytes <= maxAcceptableBytes) "SUCCESS" else "FAILURE (forced < 7KB)"

        Log.d("SignaturePad", "Save Status: Iteration Complete")
        Log.d("SignaturePad", "Final File Path: ${file.absolutePath}")
        Log.d("SignaturePad", "Final File Size: ${String.format("%.2f", finalFileSizeInKB)} KB (Quality: $currentQuality, status: $status)")

        // 6. ENCODE THE FILE TO BASE64 AND PRINT
        try {
            val bytes = file.readBytes()
            // Use Base64.DEFAULT for standard encoding
            val base64EncodedString = Base64.encodeToString(bytes, Base64.DEFAULT)

            Log.d("SignaturePad", "--- Base64 Encoded Image Data ---")
            Log.d("SignaturePad", base64EncodedString)
            Log.d("SignaturePad", "-----------------------------------")
            return base64EncodedString

        } catch (e: IOException) {
            Log.e("SignaturePad", "Error reading file for Base64 encoding: ${e.message}")
            return null
        }
    }

    /**
     * Decodes a Base64 encoded string (of an image) back into an Android Bitmap.
     *
     * @param base64String The Base64 encoded string returned by saveSignatureImage.
     * @return The decoded Bitmap, or null if decoding fails.
     */
    fun decodeBase64ToBitmap(base64String: String?): Bitmap? {
        if (base64String.isNullOrEmpty()) return null

        return try {
            // 1. Decode the Base64 string into a byte array.
            // Use Base64.DEFAULT as you did during encoding.
            val decodedBytes: ByteArray = Base64.decode(base64String, Base64.DEFAULT)

            // 2. Convert the byte array into a Bitmap.
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        } catch (e: IllegalArgumentException) {
            // This exception is typically thrown if the input string is not valid Base64
            Log.e("SignaturePad", "Error decoding Base64 string: ${e.message}")
            null
        }
    }

    fun Bitmap.toImageBitmap(): ImageBitmap {
        return this.asImageBitmap() // Uses the extension function from androidx.compose.ui.graphics
    }

    fun String?.showToastMessage(context: Context) {
        this?.let {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun isColorDark(colorValue: androidx.compose.ui.graphics.Color): Boolean {
        var color = colorValue.toArgb()
        var darkness : Double = 0.0
        darkness = try {
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
        return darkness >= 0.5
    }

    fun getTextColorBasedOnColortype(colorValue: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color {
        return if (isColorDark(colorValue)) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black
    }

    fun dialPhoneNumber(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    }

    fun getAppCode(context: Context): String {
        return if (AppSignatureHashHelper(context).appSignatures.first().isNotNullOrEmpty()) AppSignatureHashHelper(context).appSignatures.first() else ""
    }

    /* Calculates the distance between two GPS coordinates using the Haversine formula.
    *
    * @param lat1 Latitude of first point (in degrees)
    * @param lon1 Longitude of first point (in degrees)
    * @param lat2 Latitude of second point (in degrees)
    * @param lon2 Longitude of second point (in degrees)
    * @return Distance in meters
    */
    fun haversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        Log.d(TAG, "haversineDistanceMeters: lat1 "+lat1)
        Log.d(TAG, "haversineDistanceMeters: lon1 "+lon1)
        Log.d(TAG, "haversineDistanceMeters: lat2 "+lat2)
        Log.d(TAG, "haversineDistanceMeters: lon2 "+lon2)
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c // returns distance in meters
    }

    fun convertLocationStringToDouble(value :String?):Double{
        return if (value.isNullOrEmpty()){
            0.0
        }else{
            value.toDoubleOrNull() ?: 0.0
        }
    }

    /**
     * Checks if the device's main location setting (GPS or Network) is currently enabled.
     * * @param context The application or activity context required to access system services.
     * @return True if either GPS or Network location is enabled, False otherwise.
     */
    fun isDeviceLocationOn(context: Context): Boolean {
        // Get the system's LocationManager service
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check if the GPS provider is enabled
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        // Check if the Network provider (Wi-Fi/Cell towers) is enabled
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        // Return true if at least one provider is enabled
        return isGpsEnabled || isNetworkEnabled
    }

    fun saveImageToInternalStorage(context: Context, image: InputImage) {
        if (image != null){
            val bit = imageToBitmap(image)
            if (bit!= null){
                saveBitmapToInternalStorage(context,bit,"saved_image_${System.currentTimeMillis()}.png")
            }
        }
    }


     fun imageToBitmap(image: InputImage): Bitmap? {
        val frameMetadata: FrameMetadata =
            FrameMetadata.Builder()
                .setWidth(image.width)
                .setHeight(image.height)
                .setRotation(image.rotationDegrees)
                .build()
        val bitmapUtils = BitmapUtils()
        val nv21Buffer: ByteBuffer =
            bitmapUtils.yuv420ThreePlanesToNV21(image.planes, image.width, image.height)
        return bitmapUtils.getBitmap(nv21Buffer, frameMetadata)
    }
    fun saveBitmapToInternalStorage(context: Context, bitmapImage: Bitmap, filename: String): String? {
        // 1. Get a FileOutputStream for the private file
        var fileOutputStream: FileOutputStream? = null
        try {
            Log.d(TAG, "Attempting to save bitmap with filename: $filename")
            fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)

            // 2. Compress the Bitmap and write it to the OutputStream
            // Using PNG format (lossless) with 100 quality (ignored for PNG, but standard)
            val success = bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

            if (!success) {
                // Compress returned false (rare, but good to check)
                Log.e(TAG, "❌ Bitmap compression failed for unknown reason.")
                return null
            }

            // 3. Get the path for the saved file
            val savedPath = context.getFileStreamPath(filename).absolutePath

            // Log the success and the absolute path
            Log.i(TAG, "✅ Bitmap saved successfully to: $savedPath")
            return savedPath

        } catch (e: Exception) {
            // Log the failure details
            Log.e(TAG, "❌ Failed to save bitmap: ${e.message}", e)
            return null // Return null on failure
        } finally {
            // 4. Always close the stream in a 'finally' block
            try {
                fileOutputStream?.close()
                Log.d(TAG, "FileOutputStream closed.")
            } catch (e: IOException) {
                // Log if closing the stream fails
                Log.e(TAG, "Error closing FileOutputStream: ${e.message}", e)
            }
        }
    }
}