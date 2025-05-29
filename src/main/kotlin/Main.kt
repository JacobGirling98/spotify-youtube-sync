package org.example

import arrow.core.getOrElse
import org.example.config.loadEnvironmentVariables
import org.example.http.auth.SpotifyAuth
import org.example.http.auth.YouTubeAuth
import org.example.http.server.redirectHandler
import org.example.http.server.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer

fun main() {
    val environment = loadEnvironmentVariables().getOrElse { error(it.message) }
    val serverUri = "http://127.0.0.1:8000"

    val spotifyConfig = SpotifyAuth(serverUri)
    val youtubeConfig = YouTubeAuth(serverUri)

    val app = routes(
        spotifyConfig,
        youtubeConfig,
        redirectHandler { println("Spotify: $it") },
        redirectHandler { println("Youtube: $it") }
    ).asServer(Undertow(8000)).start()

    println(youtubeConfig.codeUri(environment.youtubeClientId))
    println(spotifyConfig.codeUri(environment.spotifyClientId))
}
