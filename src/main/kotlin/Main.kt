package org.example

import arrow.core.getOrElse
import org.example.config.loadEnvironmentVariables
import org.example.http.auth.spotifyAuthRequestUrl
import org.example.http.auth.youtubeAuthRequestUrl
import org.example.http.server.routes
import org.example.http.server.spotifyRedirectHandler
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.server.Undertow
import org.http4k.server.asServer

fun main() {
    val environment = loadEnvironmentVariables().getOrElse { error(it.message) }

    val app = routes(spotifyRedirectHandler { println(it) }).asServer(Undertow(8000)).start()

    val youtubeAuthUrl = youtubeAuthRequestUrl(environment.youtubeClientId)
    val spotifyAuthUrl = spotifyAuthRequestUrl(environment.spotifyClientId)

    app.stop()

    println(spotifyAuthUrl)
}