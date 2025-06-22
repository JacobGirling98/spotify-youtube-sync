package org.example.http.auth

import arrow.core.Either
import org.example.domain.error.HttpError

interface TokenManager {
    fun token(): Either<HttpError, AccessToken>
    fun updateAuthCode(authCode: AuthCode)
}