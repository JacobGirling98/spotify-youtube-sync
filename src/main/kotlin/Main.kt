package org.example

import arrow.core.getOrElse
import org.example.config.loadEnvironmentVariables
import org.example.http.auth.AuthCode
import org.example.http.auth.RefreshToken
import org.example.http.auth.SpotifyAuth
import org.example.http.auth.TokenManager
import org.example.http.auth.YouTubeAuth
import org.example.http.auth.getToken
import org.example.http.auth.refreshToken
import org.example.http.server.redirectHandler
import org.example.http.server.routes
import org.http4k.client.ApacheClient
import org.http4k.server.Undertow
import org.http4k.server.asServer
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main() {
    val environment = loadEnvironmentVariables().getOrElse { error(it.message) }
    val serverUri = "http://127.0.0.1:8000"

    val spotifyConfig = SpotifyAuth(serverUri)
    val youtubeConfig = YouTubeAuth(serverUri)

    val client = ApacheClient()

    fun spotifyFetchToken(authCode: AuthCode) = getToken(
        authCode,
        spotifyConfig.tokenUri,
        spotifyConfig.redirectUri,
        environment.spotifyClientId,
        environment.spotifyClientSecret,
        client
    )

    fun spotifyRefreshToken(refreshToken: RefreshToken) = refreshToken(
        refreshToken,
        spotifyConfig.tokenUri,
        environment.spotifyClientId,
        environment.spotifyClientSecret,
        client
    )

    fun youtubeFetchToken(authCode: AuthCode) = getToken(
        authCode,
        youtubeConfig.tokenUri,
        youtubeConfig.redirectUri,
        environment.youtubeClientId,
        environment.youtubeClientSecret,
        client
    )

    fun youtubeRefreshToken(refreshToken: RefreshToken) = refreshToken(
        refreshToken,
        youtubeConfig.tokenUri,
        environment.youtubeClientId,
        environment.youtubeClientSecret,
        client
    )

    val spotifyTokenManager = TokenManager(::spotifyFetchToken, ::spotifyRefreshToken)
    val youTubeTokenManager = TokenManager(::youtubeFetchToken, ::youtubeRefreshToken)

    println(spotifyConfig.codeUri(environment.spotifyClientId))
    println(youtubeConfig.codeUri(environment.youtubeClientId))

    val app = routes(
        spotifyConfig,
        youtubeConfig,
        redirectHandler { spotifyTokenManager.updateAuthCode(it) },
        redirectHandler { youTubeTokenManager.updateAuthCode(it) }
    ).asServer(Undertow(8000)).start()

    while(true) {
        if (spotifyTokenManager.token().leftOrNull() == null && youTubeTokenManager.token().leftOrNull() == null) {
            break
        }
    }

    println(spotifyTokenManager.token())
    println(youTubeTokenManager.token())
}
