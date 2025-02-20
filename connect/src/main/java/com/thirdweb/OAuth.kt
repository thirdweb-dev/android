
import android.util.Base64
import com.thirdweb.TokenExchangeException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.text.Charsets.UTF_8

/**
 * Utility object for handling OAuth2 PKCE (Proof Key for Code Exchange) flow.
 *
 * This implementation provides methods for generating secure random values for state
 * and code verifier, as well as computing the code challenge and handling the token exchange.
 */
object OAuth {
    private val secureRandom = SecureRandom()

    /**
     * Represents the response from the token exchange
     */
    data class TokenResponse(
        val accessToken: String,
        val tokenType: String,
        val expiresIn: Int?,
        val refreshToken: String?,
        val scope: String?
    )

    /**
     * Generates a random state parameter for OAuth2 flow.
     * The state is a 12-byte random value encoded in base64url format.
     *
     * @return A base64url encoded string of 12 random bytes
     */
    fun generateState(): String {
        val randomBytes = ByteArray(12)
        secureRandom.nextBytes(randomBytes)
        return base64UrlEncode(randomBytes)
    }

    /**
     * Generates a code verifier for PKCE.
     * The code verifier is a 64-byte random value encoded in base64url format.
     *
     * @return A base64url encoded string of 64 random bytes
     */
    fun generateCodeVerifier(): String {
        val randomBytes = ByteArray(64)
        secureRandom.nextBytes(randomBytes)
        return base64UrlEncode(randomBytes)
    }

    /**
     * Generates a code challenge from the given code verifier using SHA-256 hashing.
     *
     * @param codeVerifier The code verifier to generate the challenge from
     * @return A base64url encoded SHA-256 hash of the code verifier
     */
    fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(UTF_8)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        return base64UrlEncode(digest)
    }

    /**
     * Exchanges the authorization code for tokens using PKCE flow.
     *
     * @param loginUrl The base URL of the authorization server
     * @param code The authorization code received from the authorization server
     * @param codeVerifier The original code verifier generated for this flow
     * @param state The original state value generated for this flow
     * @param clientId The OAuth client ID
     * @param redirectUri The redirect URI used in the authorization request
     * @return A [TokenResponse] containing the token information
     * @throws TokenExchangeException if the exchange fails
     */
    suspend fun exchangeToken(
        url: String,
        code: String,
        codeVerifier: String,
        clientId: String,
        redirectUri: String
    ): TokenResponse = suspendCoroutine { continuation ->
        try {
            val uri = URL(url)
            val connection = (uri.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            // Create request body
            val requestBody = JSONObject().apply {
                put("code", code)
                put("code_verifier", codeVerifier)
                put("client_id", clientId)
                put("redirect_uri", redirectUri)
                put("grant_type", "authorization_code")
            }

            // Write request body
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray(UTF_8))
            }

            // Handle response
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    continuation.resume(TokenResponse(
                        accessToken = jsonResponse.getString("access_token"),
                        tokenType = jsonResponse.getString("token_type"),
                        expiresIn = if (jsonResponse.has("expires_in")) jsonResponse.getInt("expires_in") else null,
                        refreshToken = if (jsonResponse.has("refresh_token")) jsonResponse.getString("refresh_token") else null,
                        scope = if (jsonResponse.has("scope")) jsonResponse.getString("scope") else null
                    ))
                }
                else -> {
                    val errorStream = connection.errorStream
                    val errorMessage = errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                    continuation.resumeWithException(
                        TokenExchangeException(errorMessage, connection.responseCode)
                    )
                }
            }
        } catch (e: Exception) {
            continuation.resumeWithException(TokenExchangeException(e.message ?: "Unknown error"))
        }
    }

    /**
     * Encodes a byte array to base64url format.
     * Base64url is a URL and filename safe variant of base64 encoding.
     *
     * @param input The byte array to encode
     * @return A base64url encoded string
     */
    private fun base64UrlEncode(input: ByteArray): String {
        // Encode to base64url and remove padding characters
        return Base64.encodeToString(input, Base64.NO_WRAP or Base64.URL_SAFE)
            .replace("=", "")
    }
}