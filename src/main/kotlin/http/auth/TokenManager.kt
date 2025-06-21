package org.example.http.auth

import arrow.core.Either

interface TokenManager {
    fun token(): Either<HttpError, AccessToken>
    fun updateAuthCode(authCode: AuthCode)
}