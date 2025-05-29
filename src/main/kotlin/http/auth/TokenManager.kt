package org.example.http.auth

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@ExperimentalTime
class TokenManager(
    private var authCode: AuthCode,
    private val fetchToken: (AuthCode) -> Either<GetTokenError, Token>,
    private val refreshToken: (Token) -> Either<GetTokenError, Token>,
    private val clock: Clock
) {
    private data class TokenState(
        val token: Token,
        val expiration: Instant
    )

    private var tokenState: Either<GetTokenError, TokenState> = Either.Left(TokenNotSet)

    fun token(): Either<GetTokenError, AccessToken> {
        val token = if (tokenState.isLeft { it is TokenNotSet }) retrieveToken { fetchToken(authCode) } else tokenState

        val newToken = token.flatMap {
            if (it.expiration <= clock.now()) retrieveToken { refreshToken(it.token) } else Either.Right(it)
        }

        tokenState = newToken

        return newToken.map { it.token.accessToken }
    }

    private fun retrieveToken(fn: () -> Either<GetTokenError, Token>): Either<GetTokenError, TokenState> = either {
        val token = fn().bind()
        TokenState(token, clock.now().plus(token.expiresIn.value.seconds))
    }
}