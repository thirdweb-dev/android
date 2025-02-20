package com.thirdweb.login

import com.thirdweb.Thirdweb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ThirdwebApiService {
    @GET("contract/84532/0x638263e3eAa3917a53630e61B1fBa685308024fa/erc1155/balance-of")
    suspend fun getBalance(
        @Header("Authorization") authToken: String,
        @Query("walletAddress") walletAddress: String,
        @Query("tokenId") tokenId: String
    ): BalanceResponse

    @POST("contract/84532/0x638263e3eAa3917a53630e61B1fBa685308024fa/erc1155/claim-to")
    suspend fun claim(
        @Header("Authorization") authToken: String,
        @Header("x-backend-wallet-address") backendWalletAddress: String,
        @Header("x-account-address") accountAddress: String,
        @Body data: ClaimParams,
    ): ClaimToResponse
}


data class BalanceResponse(
    val result: Int
)

data class ClaimParams(
    val receiver: String,
    val tokenId: String,
    val quantity: String,
)

data class ClaimToResponse(
    val result: QueueResult
)

data class QueueResult(
    val queueId: String
)


object RetrofitClient {
    private const val ENGINE_URL = "your_engine_url_here"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: ThirdwebApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ENGINE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ThirdwebApiService::class.java)
    }
}

// /!\ Engine should not be called directly from your app! this is just for demo purposes /!\
// /!\ Instead you should call your own backend, using the user auth token as auth or your own auth, and call engine from your backend /!\
const val AUTH_TOKEN = "your_auth_token_here"

suspend fun getBalance(userAddress: String): Int? {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.instance.getBalance(
                authToken = "Bearer $AUTH_TOKEN",
                walletAddress = userAddress,
                tokenId = "0"
            )
            response.result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

suspend fun claim(user: Thirdweb.User): String? {
    return withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.instance.claim(
                authToken = "Bearer $AUTH_TOKEN",
                backendWalletAddress = "your_backend_wallet_address_here",
                accountAddress = user.userAddress,
                data = ClaimParams(user.userAddress, "0", "1")
            )
            response.result.queueId
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

