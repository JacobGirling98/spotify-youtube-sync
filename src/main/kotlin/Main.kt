package org.example

import arrow.core.getOrElse
import org.example.config.loadEnvironmentVariables
import org.example.http.auth.SpotifyAuth
import org.example.http.auth.YouTubeAuth
import org.example.http.server.routes
import org.example.http.server.redirectHandler
import org.http4k.server.Undertow
import org.http4k.server.asServer

fun main() {
    val environment = loadEnvironmentVariables().getOrElse { error(it.message) }

    val app = routes(redirectHandler { println(it) }).asServer(Undertow(8000)).start()

    println(YouTubeAuth.codeUri(environment.youtubeClientId))
    println(SpotifyAuth.codeUri(environment.spotifyClientId))
}
