package org.example.http.youtube.client

import arrow.core.Either
import arrow.core.raise.either
import org.example.config.bodyLens
import org.example.domain.error.Error
import org.example.domain.error.HttpError
import org.example.domain.error.HttpResponseError
import org.example.domain.error.NoResultsError
import org.example.domain.model.*
import org.example.domain.model.Service.YOUTUBE_MUSIC
import org.example.domain.music.MusicService
import org.example.http.auth.TokenManager
import org.example.http.youtube.model.Page
import org.example.http.youtube.model.PlaylistItem
import org.example.http.youtube.model.Search
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
    private val playlistItemLens = bodyLens<Page<PlaylistItem>>()
    private val searchLens = bodyLens<Page<Search>>()

    override val service: Service = YOUTUBE_MUSIC

    override fun playlists(): Either<Error, List<Playlist>> = either {
        youtubePlaylists().bind().map { playlist ->
            Playlist(
                playlist.snippet.title,
                items(playlist.id).bind()
            )
        }
    }

    override fun search(song: Song): Either<Error, SongDictionary> = either {
        val request = Request(GET, "$baseUrl/search")
            .bearerAuth(tokenManager.token().bind().value)
            .query("q", "${song.name.value} ${song.artists.joinToString(" ") { it.value }}")
            .query("part", "snippet,id")
            .query("type", "video")
        val response = http(request)

        if (!response.status.successful) raise(HttpResponseError.from(response))

        val page = catchJsonError { searchLens(response) }.bind()

        val firstResult = page.items.firstOrNull() ?: raise(NoResultsError(song))

        SongDictionary(song to ServiceIds(YOUTUBE_MUSIC to firstResult.id.videoId))
    }

    fun youtubePlaylists() = recursivePagination("$baseUrl/playlists?part=id,snippet&mine=true", null, playlistLens)

    fun items(playlistId: Id): Either<HttpError, SongDictionary> = either {
        val playlistItems = recursivePagination(
            "$baseUrl/playlistItems?part=id,snippet&playlistId=${playlistId.value}",
            null,
            playlistItemLens
        ).bind()
        SongDictionary(playlistItems.associate { item ->
            Song(item.snippet.title, listOf(item.snippet.videoOwnerChannelTitle.value.asArtist())) to ServiceIds(
                YOUTUBE_MUSIC to item.id
            )
        })
    }

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

private fun String.asArtist() = Artist(replace(" - Topic", ""))