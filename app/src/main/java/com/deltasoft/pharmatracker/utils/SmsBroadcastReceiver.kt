package com.deltasoft.pharmatracker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

class SmsBroadcastReceiver : BroadcastReceiver() {

    var onSmsReceived: ((String) -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status

            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                    message?.let {
                        val otp = extractOtpFromMessage(it)
                        otp?.let { receivedOtp ->
                            onSmsReceived?.invoke(receivedOtp)
                        }
                    }
                }
                CommonStatusCodes.TIMEOUT -> {
                    // OTP not received within 5 minutes. Handle this case (e.g., show a manual entry button).
                }
            }
        }
    }

    private fun extractOtpFromMessage(message: String): String? {
        // Regex to find the OTP. This assumes a 6-digit OTP.
        // Adjust the regex if your OTP format is different.
        val pattern = Pattern.compile("(|^)\\d{6}")
        val matcher = pattern.matcher(message)
        return if (matcher.find()) {
            matcher.group(0)
        } else {
            null
        }
    }
}