package com.thirdweb.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thirdweb.Thirdweb

class NftViewModel : ViewModel() {
    private val _balance = MutableStateFlow<Int?>(null)
    val balance: StateFlow<Int?> = _balance

    fun fetchBalance(userAddress: String) {
        viewModelScope.launch {
            _balance.value = getBalance(userAddress)
        }
    }

    fun claimNft(user: Thirdweb.User) {
        viewModelScope.launch {
            claim(user)
        }
    }
}

@Composable
fun NftView(user: Thirdweb.User, viewModel: NftViewModel = viewModel()) {
    val balance by viewModel.balance.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.fetchBalance(user.userAddress)
    }

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (balance != null) {
            Text(text = "Balance: $balance")
        } else {
            Text(text = "Loading balance...")
        }
        Button(
            onClick = {
                viewModel.claimNft(user)
            }
        ) {
            Text(text = "Claim NFT")
        }
        Button(
            onClick = {
                viewModel.fetchBalance(user.userAddress)
            }
        ) {
            Text(text = "Refresh Balance")
        }
    }
}