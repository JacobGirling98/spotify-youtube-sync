package org.example

import arrow.core.getOrElse
import org.example.config.loadEnvironmentVariables
import org.example.http.auth.AuthCode
import org.example.http.auth.getToken
import org.example.http.auth.spotifyAuthRequestUrl
import org.example.http.server.routes
import org.example.http.server.spotifyRedirectHandler
import org.http4k.client.ApacheClient
import org.http4k.server.Undertow
import org.http4k.server.asServer

fun main() {
    val environment = loadEnvironmentVariables().getOrElse { error(it.message) }

    val auth: (String) -> Unit = { code ->
        getToken(
            AuthCode(code),
            "https://accounts.spotify.com/api/token",
            "http://127.0.0.1:8000/spotify_callback",
            environment.spotifyClientId,
            environment.spotifyClientSecret,
            ApacheClient()
        ).fold(
            { left -> println(left.message) },
            { right -> println(right) }
        )
    }

    val app = routes(spotifyRedirectHandler { auth(it) }).asServer(Undertow(8000)).start()

    val spotifyAuthUrl = spotifyAuthRequestUrl(environment.spotifyClientId)

    println(spotifyAuthUrl)
}
