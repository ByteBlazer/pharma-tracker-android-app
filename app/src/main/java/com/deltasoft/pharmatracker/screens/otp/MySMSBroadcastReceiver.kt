package com.deltasoft.pharmatracker.screens.otp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

class MySMSBroadcastReceiver : BroadcastReceiver() {

    private var listener: Listener? = null

    fun initListener(listener: Listener) {
        this.listener = listener
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action==SmsRetriever.SMS_RETRIEVED_ACTION){
            val extras = intent.extras
            val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status
            when(smsRetrieverStatus.statusCode){
                CommonStatusCodes.SUCCESS->{
//                    val sms = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
//                    val otp = parseOtp(sms)
//                    listener?.onOtpReceived(otp)

                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                    message?.let {
                        val otp = extractOtpFromMessage(it)
                        otp?.let { receivedOtp ->
                            listener?.onOtpReceived(otp)
                        }
                    }
                }
                else -> {}
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

    private fun parseOtp(sms: String?): String? {
        return sms?.substring(0,6)
    }

    interface Listener {
        fun onOtpReceived(value: String?)
    }
}