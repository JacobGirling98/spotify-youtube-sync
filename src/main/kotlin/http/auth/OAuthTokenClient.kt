package org.example.http.auth

import arrow.core.Either
import org.example.domain.error.HttpResponseError
import org.example.http.util.httpResponseError
import org.example.util.catchJsonError
import org.http4k.core.*
import org.http4k.filter.ClientFilters.CustomBasicAuth.withBasicAuth
import org.http4k.lens.*

fun getToken(
    authCode: AuthCode,
    endpoint: String,
    redirectUri: String,
    clientId: String,
    clientSecret: String,
    httpClient: HttpHandler
): TokenResult {
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

    if (!response.status.successful) return httpResponseError(response)

    return catchJsonError { authCodeResponseLens(response) }
        .map {
            Token(
                AccessToken(it.access_token),
                RefreshToken(it.refresh_token),
                ExpiresIn(it.expires_in)
            )
        }
}

fun refreshToken(
    refreshToken: RefreshToken,
    endpoint: String,
    clientId: String,
    clientSecret: String,
    httpClient: HttpHandler
): TokenResult {
    val refreshTokenField = FormField.string().required("refresh_token")
    val strictFormBody = Body.webForm(Validator.Strict, grantTypeField, refreshTokenField).toLens()

    val webForm = WebForm().with(
        grantTypeField of "refresh_token",
        refreshTokenField of refreshToken.value
    )

    val request = Request(Method.POST, endpoint)
        .with(strictFormBody of webForm)
        .withBasicAuth(Credentials(clientId, clientSecret))
        .contentType(ContentType.APPLICATION_FORM_URLENCODED)

    val response = httpClient(request)

    if (!response.status.successful) return Either.Left(HttpResponseError(response.status.code, response.bodyString()))

    return catchJsonError { refreshTokenResponseLens(response) }
        .map {
            Token(
                AccessToken(it.access_token),
                refreshToken,
                ExpiresIn(it.expires_in)
            )
        }
}


