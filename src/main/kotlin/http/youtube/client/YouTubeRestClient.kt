package org.example.http.youtube.client

import arrow.core.Either
import arrow.core.raise.either
import org.example.config.bodyLens
import org.example.domain.error.Error
import org.example.domain.error.HttpError
import org.example.domain.error.HttpResponseError
import org.example.domain.model.Playlist
import org.example.domain.music.MusicService
import org.example.http.auth.TokenManager
import org.example.http.youtube.model.Page
import org.example.util.catchJsonError
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.lens.BodyLens
import org.http4k.lens.bearerAuth

class YouTubeRestClient(
    private val http: HttpHandler,
    private val tokenManager: TokenManager,
    private val baseUrl: String
) : MusicService {
    private val playlistLens = bodyLens<Page<org.example.http.youtube.model.Playlist>>()

    override fun playlists(): Either<Error, List<Playlist>> {
        TODO("Not yet implemented")
    }

    fun youtubePlaylists() = recursivePagination("$baseUrl/playlists?part=id,snippet&mine=true", null, playlistLens)

    private fun <T> recursivePagination(
        url: String,
        pageToken: String?,
        lens: BodyLens<Page<T>>
    ): Either<HttpError, List<T>> = either {
        val request = Request(GET, url).query("pageToken", pageToken).bearerAuth(tokenManager.token().bind().value)
        val response = http(request)

        if (!response.status.successful) raise(HttpResponseError.from(response))

        val page = catchJsonError { lens(response) }.bind()

        if (page.nextPageToken == null) {
            page.items
        } else {
            page.items + recursivePagination(url, page.nextPageToken, lens).bind()
        }
    }

}