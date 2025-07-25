package org.example

import arrow.core.getOrElse
import org.example.config.loadEnvironmentVariables
import org.example.domain.model.Id
import org.example.http.auth.*
import org.example.http.server.redirectHandler
import org.example.http.server.routes
import org.example.http.spotify.client.SpotifyRestClient
import org.example.http.util.retry
import org.example.http.youtube.client.YouTubeRestClient
import org.http4k.client.ApacheClient
import org.http4k.server.Undertow
import org.http4k.server.asServer
import java.time.Duration
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

    val spotifyTokenManager = OAuthTokenManager(::spotifyFetchToken, ::spotifyRefreshToken)
    val youTubeTokenManager = OAuthTokenManager(::youtubeFetchToken, ::youtubeRefreshToken)

    val app = routes(
        spotifyConfig,
        youtubeConfig,
        redirectHandler { spotifyTokenManager.updateAuthCode(it) },
        redirectHandler { youTubeTokenManager.updateAuthCode(it) }
    ).asServer(Undertow(8000)).start()

    val spotifyClient = SpotifyRestClient(retry(client), spotifyTokenManager, "https://api.spotify.com/v1")
    val youTubeRestClient =
        YouTubeRestClient(retry(client), youTubeTokenManager, "https://www.googleapis.com/youtube/v3")

    println(youtubeConfig.codeUri(environment.youtubeClientId))

    while (true) {
        Thread.sleep(Duration.ofSeconds(20))
        println(youTubeRestClient.items(Id("PLzpx5onT8uO7twspWp7VdhbEAfI8DT9oy")))
    }
}
