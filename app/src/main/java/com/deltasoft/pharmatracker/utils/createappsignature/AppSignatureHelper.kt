package com.deltasoft.pharmatracker.utils.createappsignature

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class AppSignatureHelper(private val context: Context) {

    companion object {
        private const val TAG = "AppSignatureHelper"
        private const val HASH_TYPE = "SHA-256"
        private const val NUM_HASHED_BYTES = 9
        private const val NUM_BASE64_CHAR = 11
    }

    fun getAppSignatures(): List<String> {
        val appCodes = mutableListOf<String>()
        try {
            // Get all package signatures
            val packageName = context.packageName
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            )

            for (signature in packageInfo.signatures!!) {
                val hash = hash(packageName, signature.toCharsString())
                if (hash != null) {
                    appCodes.add(hash)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Package not found", e)
        }
        return appCodes
    }

    private fun hash(packageName: String, signature: String): String? {
        val appInfo = "$packageName $signature"
        try {
            val messageDigest = MessageDigest.getInstance(HASH_TYPE)
            messageDigest.update(appInfo.toByteArray(StandardCharsets.UTF_8))
            var hashSignature = messageDigest.digest()

            // Truncate to 9 bytes
            hashSignature = hashSignature.copyOfRange(0, NUM_HASHED_BYTES)

            // Encode into Base64 and trim to 11 chars
            val base64Hash =
                Base64.encodeToString(hashSignature, Base64.NO_PADDING or Base64.NO_WRAP)
            return base64Hash.substring(0, NUM_BASE64_CHAR)
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "NoSuchAlgorithm", e)
        }
        return null
    }
}
