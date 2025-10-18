@file:OptIn(ExperimentalTime::class)

package org.example.http.spotify.client

import arrow.core.Either
import arrow.core.raise.either
import org.example.config.bodyLens
import org.example.domain.error.Error
import org.example.domain.error.HttpError
import org.example.domain.error.HttpResponseError
import org.example.domain.model.*
import org.example.domain.music.MusicService
import org.example.http.auth.TokenManager
import org.example.http.spotify.model.Page
import org.example.http.spotify.model.Playlist
import org.example.http.spotify.model.PlaylistItem
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
) : MusicService {
    private val playlistLens = bodyLens<Page<Playlist>>()
    private val trackLens = bodyLens<Page<PlaylistItem>>()

    override val service: Service = Service.SPOTIFY

    override fun playlists(): Either<HttpError, List<org.example.domain.model.Playlist>> = either {
        spotifyPlaylists().bind().map { playlist ->
            Playlist(
                playlist.name,
                tracks(playlist.id).bind()
            )
        }
    }

    override fun search(song: Song): Either<Error, SongDictionary> {
        TODO("Not yet implemented")
    }

    override fun deletePlaylist(id: Id): Either<Error, Unit> {
        TODO("Not yet implemented")
    }

    fun spotifyPlaylists(): Either<HttpError, List<Playlist>> =
        recursivePagination("$baseUrl/me/playlists", playlistLens)

    fun tracks(playlistId: Id): Either<HttpError, SongDictionary> = either {
        val playlistItems = recursivePagination("$baseUrl/playlists/${playlistId.value}/tracks", trackLens).bind()
        val tracks = playlistItems.mapNotNull { it.track }
        SongDictionary(tracks.associate { track ->
            Song(track.name, track.artists.map { artist -> Artist(artist.name.value) }) to ServiceIds(
                Service.SPOTIFY to track.id
            )
        })
    }

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

