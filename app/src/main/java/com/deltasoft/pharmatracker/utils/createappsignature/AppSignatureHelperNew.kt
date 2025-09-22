package com.deltasoft.pharmatracker.utils.createappsignature

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class AppSignatureHelperNew(private val context: Context) {

    private val HASH_TYPE = "SHA-256"
    private val NUM_HASHED_BYTES = 9
    private val NUM_BASE64_CHAR = 11

    fun getAppSignatures(): ArrayList<String> {
        val appSignatures = ArrayList<String>()
        try {
            // Get all package signatures for the current package
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )

            // For each signature, get the hash
            for (signature in packageInfo.signatures!!) {
                try {
                    val messageDigest = MessageDigest.getInstance(HASH_TYPE)
                    messageDigest.update(signature.toByteArray())

                    // Base64 encode the SHA-256 hash and get the first 11 characters
                    val signatureHash = Base64.encodeToString(
                        messageDigest.digest(),
                        Base64.NO_PADDING or Base64.NO_WRAP
                    )

                    if (signatureHash.length > NUM_BASE64_CHAR) {
                        appSignatures.add(signatureHash.substring(0, NUM_BASE64_CHAR))
                    }

                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appSignatures
    }
}