@file:OptIn(ExperimentalTime::class)

package org.example.http.spotify.client

import arrow.core.Either
import arrow.core.raise.either
import org.example.config.bodyLens
import org.example.http.auth.HttpError
import org.example.http.auth.HttpResponseError
import org.example.http.auth.TokenManager
import org.example.http.spotify.model.Page
import org.example.http.spotify.model.Playlist
import org.example.util.catchJsonError
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.lens.BodyLens
import org.http4k.lens.bearerAuth
import kotlin.time.ExperimentalTime

class SpotifyRestClient(
    private val http: HttpHandler,
    private val tokenManager: TokenManager,
    private val baseUrl: String
) {
    private val playlistLens = bodyLens<Page<Playlist>>()

    fun playlists(): Either<HttpError, List<Playlist>> = recursivePagination("$baseUrl/me/playlists", playlistLens)

    private fun <T> recursivePagination(url: String, lens: BodyLens<Page<T>>): Either<HttpError, List<T>> = either {
        val request = Request(GET, url).bearerAuth(tokenManager.token().bind().value)
        val response = http(request)

        if (!response.status.successful) raise(HttpResponseError.from(response))

        val page = catchJsonError { lens(response) }.bind()

        if (page.next == null) {
            page.items
        } else {
            page.items + recursivePagination(page.next, lens).bind()
        }
    }
}

