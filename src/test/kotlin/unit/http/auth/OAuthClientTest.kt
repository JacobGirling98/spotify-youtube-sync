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
    private val endpoint = "an-auth-endpoint"
    private val redirect = "a-redirect"
    private val clientId = "client-id"
    private val clientSecret = "client-secret"

    private val expectedAccessToken = "321"
    private val expectedRefreshToken = "654"
    private val expectedExpiresIn = "3600"


    private val validJson = """
        {
            "access_token": "$expectedAccessToken",
            "refresh_token": "$expectedRefreshToken",
            "expires_in": $expectedExpiresIn
        }
    """.trimIndent()

    @Test
    fun `sends correctly configured request and returns token`() {
        val client: HttpHandler = { request ->
            request should haveBody(
                Body.string(ContentType.APPLICATION_FORM_URLENCODED).toLens(),
                be("grant_type=authorization_code&code=$authCode&redirect_uri=$redirect")
            )
            request shouldHaveUri endpoint
            request.basicAuthentication() shouldBe Credentials(clientId, clientSecret)

            Response(OK).body(validJson)
        }

        val response = getToken(AuthCode(authCode), endpoint, redirect, clientId, clientSecret, client)

        response shouldBeRight Token(
            AccessToken(expectedAccessToken),
            RefreshToken(expectedRefreshToken),
            ExpiresIn(expectedExpiresIn.toInt())
        )
    }

    @Test
    fun `fails with UnexpectedJsonError`() {
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
    fun `fails with HttpError when request fails`() {
        val client: HttpHandler = { Response(BAD_REQUEST).body("An error") }

        val response = getToken(AuthCode(authCode), endpoint, redirect, clientId, clientSecret, client)

        response.leftOrNull() shouldBe HttpError(400, "An error")
    }
}