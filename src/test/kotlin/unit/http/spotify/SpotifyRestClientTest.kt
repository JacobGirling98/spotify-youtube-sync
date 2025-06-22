@file:OptIn(ExperimentalTime::class)

package unit.http.spotify

import fixtures.TestTokenManager
import fixtures.spotifyCurrentUserPlaylists
import fixtures.spotifyPlaylistItems
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldInclude
import io.kotest.matchers.types.shouldBeInstanceOf
import org.example.domain.model.*
import org.example.domain.model.Service.SPOTIFY
import org.example.http.auth.HttpResponseError
import org.example.http.auth.JsonError
import org.example.http.spotify.client.SpotifyRestClient
import org.example.http.spotify.model.Playlist
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import kotlin.test.Test
import kotlin.test.fail
import kotlin.time.ExperimentalTime

class SpotifyRestClientTest {

    private val song = "a song"
    private val artist = "an artist"
    private val songId = "456"
    private val playlistId = "123"

    @Test
    fun `can get playlists when only one page`() {
        val http: HttpHandler = { Response(OK).body(spotifyCurrentUserPlaylists("playlist-id", "playlist-name")) }

        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        val playlists = client.playlistIds()

        playlists shouldBeRight listOf(Playlist(Id("playlist-id"), Name("playlist-name")))
    }

    @Test
    fun `can get playlists with multiple pages`() {
        val nextLink = "next-page"
        val http: HttpHandler = {
            when (it.uri.toString()) {
                nextLink -> Response(OK).body(spotifyCurrentUserPlaylists("second-id", "second-name"))
                else -> Response(OK).body(spotifyCurrentUserPlaylists("first-id", "first-name", nextLink))
            }
        }
        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        val playlists = client.playlistIds()

        playlists shouldBeRight listOf(
            Playlist(Id("first-id"), Name("first-name")),
            Playlist(Id("second-id"), Name("second-name"))
        )
    }

    @Test
    fun `fails for playlists if json is not as expected`() {
        val nextLink = "next-page"
        val http: HttpHandler = {
            when (it.uri.toString()) {
                nextLink -> Response(OK).body("a bad body")
                else -> Response(OK).body(spotifyCurrentUserPlaylists("first-id", "first-name", nextLink))
            }
        }
        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        val playlists = client.playlistIds()

        playlists.leftOrNull().shouldBeInstanceOf<JsonError>()
    }

    @Test
    fun `token is passed to request for playlists`() {
        val http: HttpHandler = { request ->
            request.header("Authorization") shouldBe "Bearer my-token"
            Response(OK).body(spotifyCurrentUserPlaylists("playlist-id", "playlist-name"))
        }

        val client = SpotifyRestClient(http, TestTokenManager("my-token"), "spotify")

        client.playlistIds()
    }

    @Test
    fun `fails for playlists if token manager fails to return a token`() {
        val http: HttpHandler = { Response(OK).body(spotifyCurrentUserPlaylists("playlist-id", "playlist-name")) }
        val client = SpotifyRestClient(http, TestTokenManager(tokenFailure = true), "spotify")

        client.playlistIds().leftOrNull().shouldBeInstanceOf<HttpResponseError>()
    }

    @Test
    fun `fails for playlists if spotify returns a 4xx code`() {
        val http: HttpHandler = { Response(BAD_REQUEST).body("oh dear") }
        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        client.playlistIds() shouldBeLeft HttpResponseError(400, "oh dear")
    }

    @Test
    fun `can get playlist items when only one page`() {
        val http: HttpHandler = { request ->
            request.uri.toString() shouldInclude playlistId
            Response(OK).body(spotifyPlaylistItems(song, artist, songId))
        }

        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        val tracks = client.tracks(Id(playlistId))

        tracks shouldBeRight SongDictionary(
            Song(
                Name(song),
                listOf(Artist(artist))
            ) to ServiceIds(SPOTIFY to Id(songId))
        )
    }

    @Test
    fun `can get playlist items multiple pages`() {
        val nextLink = "next-page"
        val http: HttpHandler = {
            when (it.uri.toString()) {
                nextLink -> Response(OK).body(spotifyPlaylistItems("other-song", "other-artist", "other-id"))
                else -> Response(OK).body(spotifyPlaylistItems(song, artist, songId, nextLink))
            }
        }
        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        val tracks = client.tracks(Id(playlistId))

        tracks shouldBeRight SongDictionary(
            Song(Name(song), listOf(Artist(artist))) to ServiceIds(SPOTIFY to Id(songId)),
            Song(Name("other-song"), listOf(Artist("other-artist"))) to ServiceIds(SPOTIFY to Id("other-id"))
        )
    }

    @Test
    fun `fails for playlist items if json is not as expected`() {
        val nextLink = "next-page"
        val http: HttpHandler = {
            when (it.uri.toString()) {
                nextLink -> Response(OK).body("a bad body")
                else -> Response(OK).body(spotifyPlaylistItems(song, artist, songId, nextLink))
            }
        }
        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        val tracks = client.tracks(Id(playlistId))

        tracks.leftOrNull().shouldBeInstanceOf<JsonError>()
    }

    @Test
    fun `token is passed to request for playlist items`() {
        val http: HttpHandler = { request ->
            request.header("Authorization") shouldBe "Bearer my-token"
            Response(OK).body(spotifyPlaylistItems(song, artist, songId))
        }

        val client = SpotifyRestClient(http, TestTokenManager("my-token"), "spotify")

        client.tracks(Id(playlistId))
    }

    @Test
    fun `fails for playlist items if token manager fails to return a token`() {
        val http: HttpHandler = { Response(OK).body(spotifyPlaylistItems(song, artist, songId)) }
        val client = SpotifyRestClient(http, TestTokenManager(tokenFailure = true), "spotify")

        client.tracks(Id(playlistId)).leftOrNull().shouldBeInstanceOf<HttpResponseError>()
    }

    @Test
    fun `fails for playlist items if spotify returns a 4xx code`() {
        val http: HttpHandler = { Response(BAD_REQUEST).body("oh dear") }
        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        client.tracks(Id(playlistId)) shouldBeLeft HttpResponseError(400, "oh dear")
    }

    @Test
    fun `can get playlists in core domain`() {
        val http: HttpHandler = { request ->
            val body = when {
                request.uri.toString().contains("tracks") -> spotifyPlaylistItems(song, artist, songId)
                request.uri.toString().contains("me") -> spotifyCurrentUserPlaylists(playlistId, "playlist-name")
                else -> fail("Unknown URL hit")
            }
            Response(OK).body(body)
        }
        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        val playlists = client.playlists()

        playlists shouldBeRight listOf(
            Playlist(
                Name("playlist-name"),
                SongDictionary(Song(Name(song), listOf(Artist(artist))) to ServiceIds(SPOTIFY to Id(songId))),
            )
        )
    }
}