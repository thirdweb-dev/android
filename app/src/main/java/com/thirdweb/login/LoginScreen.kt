package com.thirdweb.login

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thirdweb.Thirdweb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



class LoginViewModel : ViewModel() {
    private val _user = MutableStateFlow<Thirdweb.User?>(null)
    val user: StateFlow<Thirdweb.User?> = _user


    fun loadUser(context: Context) {
        _user.value = thirdweb.getUser(context)
    }

    fun logout() {
        _user.value = null
    }
}

@Composable
fun LoginScreen(initialUser: Thirdweb.User?, loginViewModel: LoginViewModel = viewModel()) {
    val context = LocalContext.current
    val user by loginViewModel.user.collectAsState(initialUser)
    loginViewModel.loadUser(context)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (user == null) {
            // User is not logged in - Show login button
            Button(
                onClick = {
                    thirdweb.login(context) // Call the login function
                }
            ) {
                Text(
                    text = "Login with thirdweb",
                    fontSize = 16.sp
                )
            }
        } else {
            // User is logged in - Show user address and logout button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Logged in as:",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = user!!.userAddress,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                NftView(user!!)
                Button(
                    onClick = {
                        thirdweb.logout(context) // Call the logout function
                        loginViewModel.logout()
                    }
                ) {
                    Text(text = "Logout")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(null)
}