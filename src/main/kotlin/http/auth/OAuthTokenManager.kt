package org.example.http.auth

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import org.example.domain.error.AuthCodeNotSet
import org.example.domain.error.GetTokenError
import org.example.domain.error.HttpError
import org.example.domain.error.TokenNotSet
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@ExperimentalTime
class OAuthTokenManager(
    private val fetchToken: (AuthCode) -> TokenResult,
    private val refreshToken: (RefreshToken) -> TokenResult,
    private val clock: Clock = Clock.System
) : TokenManager {
    private data class TokenState(
        val token: Token,
        val expiration: Instant
    )

    private var authCode: Either<AuthCodeNotSet, AuthCode> = Either.Left(AuthCodeNotSet)
    private var tokenState: Either<GetTokenError, TokenState> = Either.Left(TokenNotSet)

    override fun token(): Either<HttpError, AccessToken> = either {
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

    override fun updateAuthCode(authCode: AuthCode) {
        this.authCode = Either.Right(authCode)
    }

    private inline fun retrieveToken(fn: () -> TokenResult) = either {
        fn().bind().let { TokenState(it, clock.now().plus(it.expiresIn.value.seconds)) }
    }

}