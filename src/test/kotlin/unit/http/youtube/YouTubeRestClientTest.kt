package unit.http.youtube

import fixtures.TestTokenManager
import fixtures.youTubeCurrentUserPlaylists
import fixtures.youTubePlaylistItems
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldInclude
import io.kotest.matchers.types.shouldBeInstanceOf
import org.example.domain.error.HttpResponseError
import org.example.domain.error.JsonError
import org.example.domain.model.Artist
import org.example.domain.model.Id
import org.example.domain.model.Name
import org.example.domain.model.Service.YOUTUBE_MUSIC
import org.example.domain.model.ServiceIds
import org.example.domain.model.Song
import org.example.domain.model.SongDictionary
import org.example.http.youtube.client.YouTubeRestClient
import org.example.http.youtube.model.Playlist
import org.example.http.youtube.model.PlaylistSnippet
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import kotlin.test.Test
import kotlin.test.fail

class YouTubeRestClientTest {

    private val song = "a song"
    private val artist = "an artist"
    private val songId = "456"
    private val playlistId = "123"
    
    @Test
    fun `can get playlists when only one page`() {
        val http: HttpHandler = { Response(OK).body(youTubeCurrentUserPlaylists("playlist-id", "playlist-name")) }

        val client = YouTubeRestClient(http, TestTokenManager(), "youtube")

        val playlists = client.youtubePlaylists()

        playlists shouldBeRight listOf(Playlist(Id("playlist-id"), PlaylistSnippet(Name("playlist-name"))))
    }

    @Test
    fun `can get playlists with multiple pages`() {
        val nextLink = "next-page"
        val http: HttpHandler = {
            if (it.uri.toString().contains(nextLink)) Response(OK).body(
                youTubeCurrentUserPlaylists(
                    "second-id",
                    "second-name"
                )
            )
            else Response(OK).body(youTubeCurrentUserPlaylists("first-id", "first-name", nextLink))
        }
        val client = YouTubeRestClient(http, TestTokenManager(), "youtube")

        val playlists = client.youtubePlaylists()

        playlists shouldBeRight listOf(
            Playlist(Id("first-id"), PlaylistSnippet(Name("first-name"))),
            Playlist(Id("second-id"), PlaylistSnippet(Name("second-name")))
        )
    }

    @Test
    fun `fails for playlists if json is not as expected`() {
        val nextLink = "next-page"
        val http: HttpHandler = {
            if (it.uri.toString().contains(nextLink)) Response(OK).body("a bad body")
            else Response(OK).body(youTubeCurrentUserPlaylists("first-id", "first-name", nextLink))
        }
        val client = YouTubeRestClient(http, TestTokenManager(), "youtube")

        val playlists = client.youtubePlaylists()

        playlists.leftOrNull().shouldBeInstanceOf<JsonError>()
    }

    @Test
    fun `token is passed to request for playlists`() {
        val http: HttpHandler = { request ->
            request.header("Authorization") shouldBe "Bearer my-token"
            Response(OK).body(youTubeCurrentUserPlaylists("playlist-id", "playlist-name"))
        }

        val client = YouTubeRestClient(http, TestTokenManager("my-token"), "youtube")

        client.youtubePlaylists()
    }

    @Test
    fun `fails for playlists if token manager fails to return a token`() {
        val http: HttpHandler = { Response(OK).body(youTubeCurrentUserPlaylists("playlist-id", "playlist-name")) }
        val client = YouTubeRestClient(http, TestTokenManager(tokenFailure = true), "youtube")

        client.youtubePlaylists().leftOrNull().shouldBeInstanceOf<HttpResponseError>()
    }

    @Test
    fun `fails for playlists if youtube returns a 4xx code`() {
        val http: HttpHandler = { Response(BAD_REQUEST).body("oh dear") }
        val client = YouTubeRestClient(http, TestTokenManager(), "youtube")

        client.youtubePlaylists() shouldBeLeft HttpResponseError(400, "oh dear")
    }

    @Test
    fun `can get playlist items when only one page`() {
        val http: HttpHandler = { request ->
            request.uri.toString() shouldInclude playlistId
            Response(OK).body(youTubePlaylistItems(song, artist, songId))
        }

        val client = YouTubeRestClient(http, TestTokenManager(), "youtube")

        val tracks = client.items(Id(playlistId))

        tracks shouldBeRight SongDictionary(
            Song(
                Name(song),
                listOf(Artist(artist))
            ) to ServiceIds(YOUTUBE_MUSIC to Id(songId))
        )
    }

    @Test
    fun `can get playlist items multiple pages`() {
        val nextLink = "next-page"
        val http: HttpHandler = {
            if (it.uri.toString().contains(nextLink)) Response(OK).body(youTubePlaylistItems("other-song", "other-artist", "other-id"))
            else Response(OK).body(youTubePlaylistItems(song, artist, songId, nextLink))
        }
        val client = YouTubeRestClient(http, TestTokenManager(), "youtube")

        val tracks = client.items(Id(playlistId))

        tracks shouldBeRight SongDictionary(
            Song(Name(song), listOf(Artist(artist))) to ServiceIds(YOUTUBE_MUSIC to Id(songId)),
            Song(Name("other-song"), listOf(Artist("other-artist"))) to ServiceIds(YOUTUBE_MUSIC to Id("other-id"))
        )
    }

    @Test
    fun `fails for playlist items if json is not as expected`() {
        val nextLink = "next-page"
        val http: HttpHandler = {
            if (it.uri.toString().contains(nextLink)) Response(OK).body("a bad body")
            else Response(OK).body(youTubePlaylistItems(song, artist, songId, nextLink))
        }
        val client = YouTubeRestClient(http, TestTokenManager(), "youtube")

        val tracks = client.items(Id(playlistId))

        tracks.leftOrNull().shouldBeInstanceOf<JsonError>()
    }

    @Test
    fun `token is passed to request for playlist items`() {
        val http: HttpHandler = { request ->
            request.header("Authorization") shouldBe "Bearer my-token"
            Response(OK).body(youTubePlaylistItems(song, artist, songId))
        }

        val client = YouTubeRestClient(http, TestTokenManager("my-token"), "youtube")

        client.items(Id(playlistId))
    }

    @Test
    fun `fails for playlist items if token manager fails to return a token`() {
        val http: HttpHandler = { Response(OK).body(youTubePlaylistItems(song, artist, songId)) }
        val client = YouTubeRestClient(http, TestTokenManager(tokenFailure = true), "youtube")

        client.items(Id(playlistId)).leftOrNull().shouldBeInstanceOf<HttpResponseError>()
    }

    @Test
    fun `fails for playlist items if spotify returns a 4xx code`() {
        val http: HttpHandler = { Response(BAD_REQUEST).body("oh dear") }
        val client = YouTubeRestClient(http, TestTokenManager(), "youtube")

        client.items(Id(playlistId)) shouldBeLeft HttpResponseError(400, "oh dear")
    }

    @Test
    fun `can get playlists in core domain`() {
        val http: HttpHandler = { request ->
            val body = when {
                request.uri.toString().contains("Items") -> youTubePlaylistItems(song, artist, songId)
                request.uri.toString().contains("mine") -> youTubeCurrentUserPlaylists(playlistId, "playlist-name")
                else -> fail("Unknown URL hit")
            }
            Response(OK).body(body)
        }
        val client = YouTubeRestClient(http, TestTokenManager(), "youtube")

        val playlists = client.playlists()

        playlists shouldBeRight listOf(
            org.example.domain.model.Playlist(
                Name("playlist-name"),
                SongDictionary(Song(Name(song), listOf(Artist(artist))) to ServiceIds(YOUTUBE_MUSIC to Id(songId))),
            )
        )
    }
}