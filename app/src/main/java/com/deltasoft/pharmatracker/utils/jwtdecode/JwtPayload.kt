package com.deltasoft.pharmatracker.utils.jwtdecode

/**
 * Data class to represent the structure of the JWT payload.
 * The field names must exactly match the keys in the JSON payload.
 */
data class JwtPayload(
    val id: String,
    val username: String,
    val mobile: String,
    val iat: Long,
    val exp: Long
)