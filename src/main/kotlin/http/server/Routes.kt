package org.example.http.server

import org.example.http.auth.SpotifyAuth
import org.example.http.auth.YouTubeAuth
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.routing.bind
import org.http4k.routing.routes

fun routes(
    spotifyConfig: SpotifyAuth,
    youtubeConfig: YouTubeAuth,
    spotifyRedirectHandler: HttpHandler,
    youtubeRedirectHandler: HttpHandler
): HttpHandler = routes(
    spotifyConfig.internalRedirectRoute bind GET to spotifyRedirectHandler,
    youtubeConfig.internalRedirectRoute bind GET to youtubeRedirectHandler
)
