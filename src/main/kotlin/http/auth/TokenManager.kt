package org.example.http.auth

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@ExperimentalTime
class TokenManager(
    private val fetchToken: (AuthCode) -> TokenResult,
    private val refreshToken: (RefreshToken) -> TokenResult,
    private val clock: Clock = Clock.System
) {
    private data class TokenState(
        val token: Token,
        val expiration: Instant
    )

    private var authCode: Either<AuthCodeNotSet, AuthCode> = Either.Left(AuthCodeNotSet)
    private var tokenState: Either<GetTokenError, TokenState> = Either.Left(TokenNotSet)

    fun token(): Either<GetTokenError, AccessToken> = either {
        val authCode = authCode.bind()
        val state = tokenState.getOrElse { retrieveToken { fetchToken(authCode) }.bind() }

        val refreshedState = if (state.expiration <= clock.now()) {
            retrieveToken { refreshToken(state.token.refreshToken) }.bind()
        } else {
            state
        }

        tokenState = Either.Right(refreshedState)
        refreshedState.token.accessToken
    }

    fun updateAuthCode(authCode: AuthCode) {
        this.authCode = Either.Right(authCode)
    }

    private inline fun retrieveToken(fn: () -> TokenResult) = either {
        fn().bind().let { TokenState(it, clock.now().plus(it.expiresIn.value.seconds)) }
    }

}