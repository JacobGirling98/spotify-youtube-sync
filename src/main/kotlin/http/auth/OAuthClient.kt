package org.example.http.auth

import arrow.core.Either
import arrow.core.raise.either
import org.http4k.core.*
import org.http4k.filter.ClientFilters.CustomBasicAuth.withBasicAuth
import org.http4k.lens.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

sealed class GetTokenError(open val message: String?)
data class HttpError(val statusCode: Int, override val message: String) : GetTokenError(message)
data class JsonError(override val message: String?) : GetTokenError(message)

fun getToken(
    authCode: AuthCode,
    endpoint: String,
    redirectUri: String,
    clientId: String,
    clientSecret: String,
    httpClient: HttpHandler
): Either<GetTokenError, Token> {
    val grantTypeField = FormField.string().required("grant_type")
    val codeField = FormField.string().required("code")
    val redirectUriField = FormField.string().required("redirect_uri")
    val strictFormBody = Body.webForm(Validator.Strict, grantTypeField, codeField, redirectUriField).toLens()

    val webForm = WebForm().with(
        grantTypeField of "authorization_code",
        codeField of authCode.value,
        redirectUriField of redirectUri
    )

    val request = Request(Method.POST, endpoint)
        .with(strictFormBody of webForm)
        .withBasicAuth(Credentials(clientId, clientSecret))
        .contentType(ContentType.APPLICATION_FORM_URLENCODED)

    val response = httpClient(request)

    if (!response.status.successful) return Either.Left(HttpError(response.status.code, response.bodyString()))

    return Either.catch { authResponseLens(response) }
        .mapLeft { JsonError(it.message) }
        .map {
            Token(
                AccessToken(it.access_token),
                RefreshToken(it.refresh_token),
                ExpiresIn(it.expires_in)
            )
        }

}

// fetch token request with initial code
// fetch token request with refresh token
// Token manager stores state, calls refresh periodically, and returns the value of the access token

// sealed Hierarchy of reasons token might be missing, instead of null

class TokenManager @OptIn(ExperimentalTime::class) constructor(
    private val initialAccessCode: String,
    private val fetchToken: (String) -> Token,
    private val refreshToken: (Token) -> Token,
    private val clock: Clock
) {
    private var currentToken: Token? = null
    private var tokenExpiration: Long = 0

    val token: AccessToken?
        get() = currentToken?.accessToken
}