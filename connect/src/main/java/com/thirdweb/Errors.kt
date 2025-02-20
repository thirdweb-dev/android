package com.thirdweb

/**
 * Exception thrown when token exchange fails
 */
class TokenExchangeException(message: String, val responseCode: Int? = null) : Exception(message)