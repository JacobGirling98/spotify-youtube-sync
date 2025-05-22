package org.example.http.server

import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.routing.bind
import org.http4k.routing.routes

fun routes(spotifyRedirectHandler: HttpHandler): HttpHandler = routes(
    "/spotify_callback" bind GET to spotifyRedirectHandler
)
