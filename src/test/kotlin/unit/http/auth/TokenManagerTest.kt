package unit.http.auth

import arrow.core.Either
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import org.example.http.auth.*
import util.TestClock
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
class TokenManagerTest {

    private val authCode = AuthCode("abc")
    private val anAccessToken = AccessToken("123")
    private val aRefreshedAccessToken = AccessToken("321")
    private val expiresInSeconds = 50
    private val aToken = Token(
        anAccessToken,
        RefreshToken("456"),
        ExpiresIn(expiresInSeconds)
    )
    private val fetchToken: (AuthCode) -> TokenResult = { Either.Right(aToken) }
    private val refreshToken: (RefreshToken) -> TokenResult = {
        Either.Right(
            Token(
                aRefreshedAccessToken,
                RefreshToken("456"),
                ExpiresIn(expiresInSeconds)
            )
        )
    }

    @Test
    fun `calls fetchToken if the value of the token is null`() {
        val clock = TestClock()
        val manager = tokenManagerWithAuthCode(authCode, fetchToken, refreshToken, clock)

        manager.token() shouldBeRight anAccessToken
    }

    @Test
    fun `refreshes the token if it has expired`() {
        val clock = TestClock()
        val manager = tokenManagerWithAuthCode(authCode, fetchToken, refreshToken, clock)

        manager.token() shouldBeRight anAccessToken

        clock.advanceTime(60.seconds)

        manager.token() shouldBeRight aRefreshedAccessToken
    }

    @Test
    fun `doesn't refresh if token has not expired`() {
        val clock = TestClock()
        val manager = tokenManagerWithAuthCode(authCode, fetchToken, refreshToken, clock)

        manager.token() shouldBeRight anAccessToken

        clock.advanceTime(30.seconds)

        manager.token() shouldBeRight anAccessToken
    }

    @Test
    fun `if fetch token returns an error then that is propagated out`() {
        val error = HttpResponseError(400, "Oh dear")
        val manager = tokenManagerWithAuthCode(authCode, { Either.Left(error) }, refreshToken, TestClock())

        manager.token() shouldBeLeft error
    }

    @Test
    fun `if refresh token returns an error then that is propagated out`() {
        val error = JsonError("Oh dear")
        val clock = TestClock()
        val manager = tokenManagerWithAuthCode(authCode, fetchToken, { Either.Left(error) }, clock)

        manager.token() shouldBeRight anAccessToken

        clock.advanceTime(60.seconds)

        manager.token() shouldBeLeft error
    }

    @Test
    fun `auth token is required`() {
        val manager = OAuthTokenManager(fetchToken, refreshToken, TestClock())

        manager.token() shouldBeLeft AuthCodeNotSet
    }

    @Test
    fun `can update the authCode`() {
        val fetchToken: (AuthCode) -> TokenResult = {
            it shouldBe AuthCode("abc")
            Either.Right(aToken)
        }

        val manager = tokenManagerWithAuthCode(authCode, fetchToken, refreshToken, TestClock())

        manager.updateAuthCode(AuthCode("abc"))

        manager.token() shouldBeRight anAccessToken
    }

    private fun tokenManagerWithAuthCode(
        authCode: AuthCode,
        fetchToken: (AuthCode) -> TokenResult,
        refreshToken: (RefreshToken) -> TokenResult,
        clock: Clock
    ): OAuthTokenManager {
        val tokenManager = OAuthTokenManager(fetchToken, refreshToken, clock)
        tokenManager.updateAuthCode(authCode)
        return tokenManager
    }
}

