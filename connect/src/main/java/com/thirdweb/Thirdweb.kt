package com.thirdweb

import OAuth
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

class Thirdweb(
    private val clientId: String,
    private val redirectUrl: String,
    private val baseUrl: String = "https://login.thirdweb.com"
) {

    data class User(
        val userAddress: String,
    )

    @Serializable
    data class AuthToken(
        val userAddress: String,
        val accessToken: String,
        val refreshToken: String,
    )

    private var state: String = ""
    private var codeVerifier = ""

    fun login(context: Context) {
        state = OAuth.generateState()
        codeVerifier = OAuth.generateCodeVerifier()
        val codeChallenge = OAuth.generateCodeChallenge(codeVerifier)
        val url =
            "${baseUrl}/authorize?client_id=${clientId}&redirect_uri=${redirectUrl}&response_type=code&state=${state}&code_challenge=${codeChallenge}&code_challenge_method=S256"
        // Build the Custom Chrome Tab intent
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    fun isLoginCallbackIntent(intent: Intent?): Boolean {
        val data: Uri? = intent?.data
        if (data != null) {
            val code = data.getQueryParameter("code")
            val state = data.getQueryParameter("state")
            return state != null && code != null
        }
        return false
    }

    fun handleLoginCallback(context: Context, intent: Intent?, onSuccess: (user: User, authToken: AuthToken) -> Unit, onError: (Exception) -> Unit) {
        val data: Uri = intent?.data ?: return
        val code = data.getQueryParameter("code")
        val state = data.getQueryParameter("state")

        if (state == null || code == null) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleLoginCallback(context, intent)
                withContext(Dispatchers.Main) {
                    onSuccess(getUser(context)!!, getAuthToken(context)!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    fun logout(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.remove("userAddress")
        editor.remove("sessionKeyAddress")
        editor.remove("code")
        editor.apply()
    }

    fun getUser(context: Context): User? {
        val authToken = getAuthToken(context)
        if (authToken != null) {
            return User(authToken.userAddress)
        }
        return null;
    }

    fun getAuthToken(context: Context): AuthToken? {
        return loadAuthToken(context)
    }

    private suspend fun handleLoginCallback(context: Context, intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null) {
            val code = data.getQueryParameter("code")
            val state = data.getQueryParameter("state")
            if (this.state != state) {
                throw Exception("Invalid state")
            }
            if (code != null) {
                val result = OAuth.exchangeToken(
                    url = "${baseUrl}/api/token",
                    code = code,
                    codeVerifier = codeVerifier,
                    redirectUri = redirectUrl,
                    clientId = clientId,
                )

                val decodedAccessToken = JWTDecoder.decode(result.accessToken)
                val userAddress = decodedAccessToken.sub
                saveAuthToken(context, userAddress, result)
            }
        }
    }
}

private fun saveAuthToken(
    context: Context,
    userAddress: String,
    authToken: OAuth.TokenResponse
) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    val editor: SharedPreferences.Editor = sharedPreferences.edit()
    editor.putString("userAddress", userAddress)
    editor.putString("tokenType", authToken.tokenType)
    editor.putString("accessToken", authToken.accessToken)
    editor.putString("refreshToken", authToken.refreshToken)
    authToken.expiresIn?.let { editor.putInt("expiresIn", it) }
    editor.apply() // Save asynchronously
}

// Retrieve the saved token
private fun loadAuthToken(context: Context): Thirdweb.AuthToken? {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    // Retrieve the fields of AuthToken
    val userAddress = sharedPreferences.getString("userAddress", null)
    val accessToken = sharedPreferences.getString("accessToken", null)
    val refreshToken = sharedPreferences.getString("refreshToken", null)

    return if (userAddress != null && accessToken != null && refreshToken != null) {
        Thirdweb.AuthToken(userAddress, accessToken, refreshToken)
    } else {
        null
    }
}
