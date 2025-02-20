package com.thirdweb

import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Date

@Serializable
data class JWTHeader(
    val alg: String,
    val typ: String
)

@Serializable
data class JWTPayload(
    val sub: String,       // subject
    val iat: Long,         // issued at
    val exp: Long          // expiration
    // TODO add permissions
)

/**
 * example
 * {
 *   "sub": "0x82a0fed74a731D619C948d51b482609392725a7c",
 *   "scope": "",
 *   "permissions": {
 *     "identity:read": true,
 *     "contracts:write": true,
 *     "native:spend": 5,
 *     "expiration": "2025-03-21T23:14:16.355Z"
 *   },
 *   "data": {},
 *   "iat": 1740008764,
 *   "exp": 1740009664
 * }
 */

class JWTDecodeException(message: String) : Exception(message)

object JWTDecoder {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Decodes a JWT token without signature verification
     * @param token The JWT token to decode
     * @return JWTPayload object containing the decoded claims
     * @throws JWTDecodeException if token format is invalid
     */
    fun decode(token: String): JWTPayload {
        try {
            val parts = token.split(".")
            if (parts.size != 3) {
                throw JWTDecodeException("Invalid JWT format")
            }

            val header = decodeBase64(parts[0])
            val payload = decodeBase64(parts[1])

            // Parse header to verify format
            val jwtHeader = json.decodeFromString<JWTHeader>(header)
            if (jwtHeader.typ != "JWT") {
                throw JWTDecodeException("Invalid token type")
            }

            // Parse and return payload
            return json.decodeFromString<JWTPayload>(payload)
        } catch (e: Exception) {
            when (e) {
                is JWTDecodeException -> throw e
                else -> throw JWTDecodeException("Failed to decode JWT: ${e.message}")
            }
        }
    }

    /**
     * Decodes Base64URL to String
     */
    private fun decodeBase64(str: String): String {
        val bytes = Base64.decode(str.padBase64(), Base64.URL_SAFE)
        return String(bytes, Charsets.UTF_8)
    }

    /**
     * Adds padding to Base64 if necessary
     */
    private fun String.padBase64(): String {
        val padding = when (length % 4) {
            0 -> ""
            1 -> "==="
            2 -> "=="
            3 -> "="
            else -> throw JWTDecodeException("Invalid Base64 string")
        }
        return this + padding
    }
}

// Extension function to check if token is expired
fun JWTPayload.isExpired(): Boolean {
    return Date().time >= exp * 1000
}
