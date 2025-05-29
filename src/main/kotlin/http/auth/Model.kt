package org.example.http.auth

import org.example.config.bodyLens

@Suppress("PropertyName")
data class AuthCodeResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Int
)

@Suppress("PropertyName")
data class RefreshTokenResponse(
    val access_token: String,
    val expires_in: Int,
    val refresh_token_expires_in: Int?
)

val authCodeResponseLens = bodyLens<AuthCodeResponse>()
val refreshTokenResponseLens = bodyLens<RefreshTokenResponse>()

@JvmInline
value class AuthCode(val value: String)

@JvmInline
value class AccessToken(val value: String)

@JvmInline
value class RefreshToken(val value: String)

@JvmInline
value class ExpiresIn(val value: Int)

data class Token(val accessToken: AccessToken, val refreshToken: RefreshToken, val expiresIn: ExpiresIn)

