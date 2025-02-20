package com.thirdweb.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.thirdweb.Thirdweb
import com.thirdweb.TokenExchangeException
import com.thirdweb.login.ui.theme.ThirdwebLoginTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        showUI()
        handleLoginCallback(intent)
    }

    private fun handleLoginCallback(intent: Intent?) {
        if (!thirdweb.isLoginCallbackIntent(intent)) {
            return
        }
        thirdweb.handleLoginCallback(baseContext, intent,
            onSuccess = { user, authToken ->
                Toast.makeText(
                    this@MainActivity,
                    "Logged in as: ${user.userAddress}",
                    Toast.LENGTH_LONG
                ).show()
                showUI(user)
            },
            onError = { exception ->
                val ex = exception as TokenExchangeException;
                Toast.makeText(
                    this@MainActivity,
                    "Error ${ex.responseCode}: ${ex.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    private fun showUI(user: Thirdweb.User? = null) {
        setContent {
            ThirdwebLoginTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        LoginScreen(user)
                    }
                }
            }
        }
    }
}
