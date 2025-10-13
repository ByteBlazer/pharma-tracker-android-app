package com.deltasoft.pharmatracker.utils.jwtdecode

import android.util.Base64
import com.google.gson.Gson
import java.io.UnsupportedEncodingException
import java.util.Date


object JwtDecodeUtil {
    /**
     * Decodes the payload of a JWT token.
     * @param token The JWT token string (e.g., "header.payload.signature").
     * @return A JwtPayload object if successful, or null if the token is invalid or decoding fails.
     */
    fun decodeJwtPayload(token: String): JwtPayload? {
        try {
            // Split the token into its three parts: header, payload, and signature
            val parts = token.split(".")
            if (parts.size != 3) {
                println("Invalid token format: Must contain 3 parts.")
                return null
            }

            // The payload is the second part (index 1)
            val payload = parts[1]

            // JWTs use URL-safe Base64, so we must specify this flag
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes, Charsets.UTF_8)

            // Use Gson to parse the JSON string into the JwtPayload data class
            val gson = Gson()
            return gson.fromJson(decodedString, JwtPayload::class.java)

        } catch (e: IllegalArgumentException) {
            println("Decoding failed: Malformed Base64 string.")
            e.printStackTrace()
            return null
        } catch (e: UnsupportedEncodingException) {
            println("Decoding failed: Unsupported encoding.")
            e.printStackTrace()
            return null
        } catch (e: Exception) {
            // Catch any other exceptions, like Gson parsing errors
            println("An unexpected error occurred during decoding.")
            e.printStackTrace()
            return null
        }
    }
}