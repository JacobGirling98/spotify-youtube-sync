package unit.http.auth

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.be
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.example.http.auth.*
import org.http4k.core.*
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.kotest.haveBody
import org.http4k.kotest.shouldHaveUri
import org.http4k.lens.basicAuthentication
import org.http4k.lens.string
import kotlin.test.Test

class OAuthClientTest {
    private val authCode = "123"
    private val refreshToken = "456"
    private val endpoint = "an-auth-endpoint"
    private val redirect = "a-redirect"
    private val clientId = "client-id"
    private val clientSecret = "client-secret"
    private val accessToken = "321"
    private val expiresIn = "3600"
    private val refreshTokenExpiresIn = "640000"


    private val authCodeJson = """
        {
            "access_token": "$accessToken",
            "refresh_token": "$refreshToken",
            "expires_in": $expiresIn
        }
    """.trimIndent()

    private val refreshTokenJson = """
        {
          "access_token" : "$accessToken",
          "expires_in" : $expiresIn,
          "scope" : "https://www.googleapis.com/auth/youtube.readonly",
          "token_type" : "Bearer",
          "refresh_token_expires_in" : $refreshTokenExpiresIn
        }
    """.trimIndent()

    private val refreshTokenWithoutExpiryJson = """
        {
          "access_token" : "$accessToken",
          "expires_in" : $expiresIn,
          "scope" : "https://www.googleapis.com/auth/youtube.readonly",
          "token_type" : "Bearer"
        }
    """.trimIndent()

    @Test
    fun `sends correctly configured request and returns token when using an auth code`() {
        val client: HttpHandler = { request ->
            request should haveBody(
                Body.string(ContentType.APPLICATION_FORM_URLENCODED).toLens(),
                be("grant_type=authorization_code&code=$authCode&redirect_uri=$redirect")
            )
            request shouldHaveUri endpoint
            request.basicAuthentication() shouldBe Credentials(clientId, clientSecret)

            Response(OK).body(authCodeJson)
        }

        val response = getToken(AuthCode(authCode), endpoint, redirect, clientId, clientSecret, client)

        response shouldBeRight Token(
            AccessToken(accessToken),
            RefreshToken(refreshToken),
            ExpiresIn(expiresIn.toInt())
        )
    }

    @Test
    fun `fails with JsonError when using an auth code`() {
        val json = """
                {
                    "access_token": "321"
                }
            """.trimIndent()

        val client: HttpHandler = { Response(OK).body(json) }

        val response = getToken(AuthCode(authCode), endpoint, redirect, clientId, clientSecret, client)

        response.leftOrNull().shouldBeInstanceOf<JsonError>()
    }

    @Test
    fun `fails with HttpError when request fails when using an auth code`() {
        val client: HttpHandler = { Response(BAD_REQUEST).body("An error") }

        val response = getToken(AuthCode(authCode), endpoint, redirect, clientId, clientSecret, client)

        response.leftOrNull() shouldBe HttpResponseError(400, "An error")
    }

    @Test
    fun `sends correctly configured request and returns token when using refresh token`() {
        val client: HttpHandler = { request ->
            request should haveBody(
                Body.string(ContentType.APPLICATION_FORM_URLENCODED).toLens(),
                be("grant_type=refresh_token&refresh_token=$refreshToken")
            )
            request shouldHaveUri endpoint
            request.basicAuthentication() shouldBe Credentials(clientId, clientSecret)

            Response(OK).body(refreshTokenJson)
        }

        val response = refreshToken(RefreshToken(refreshToken), endpoint, clientId, clientSecret, client)

        response shouldBeRight Token(
            AccessToken(accessToken),
            RefreshToken(refreshToken),
            ExpiresIn(expiresIn.toInt())
        )
    }

    @Test
    fun `calling refresh token works when response does not include refresh token expiry time`() {
        val client: HttpHandler = { request -> Response(OK).body(refreshTokenWithoutExpiryJson) }

        val response = refreshToken(RefreshToken(refreshToken), endpoint, clientId, clientSecret, client)

        response shouldBeRight Token(
            AccessToken(accessToken),
            RefreshToken(refreshToken),
            ExpiresIn(expiresIn.toInt())
        )
    }

    @Test
    fun `fails with JsonError when using refresh token`() {
        val json = """
                {
                    "access_token": "321"
                }
            """.trimIndent()

        val client: HttpHandler = { Response(OK).body(json) }

        val response = refreshToken(RefreshToken(refreshToken), endpoint, clientId, clientSecret, client)

        response.leftOrNull().shouldBeInstanceOf<JsonError>()
    }

    @Test
    fun `fails with HttpError when request fails when using refresh token`() {
        val client: HttpHandler = { Response(BAD_REQUEST).body("An error") }

        val response = refreshToken(RefreshToken(refreshToken), endpoint, clientId, clientSecret, client)

        response.leftOrNull() shouldBe HttpResponseError(400, "An error")
    }
}