@file:OptIn(ExperimentalTime::class)

package unit.http.spotify

import fixtures.TestTokenManager
import fixtures.spotifyCurrentUserPlaylists
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.example.domain.model.Id
import org.example.domain.model.Name
import org.example.http.auth.HttpResponseError
import org.example.http.auth.JsonError
import org.example.http.spotify.client.SpotifyRestClient
import org.example.http.spotify.model.Playlist
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import kotlin.test.Test
import kotlin.time.ExperimentalTime

class SpotifyRestClientTest {

    @Test
    fun `can get playlists when only one page`() {
        val http: HttpHandler = { Response(OK).body(spotifyCurrentUserPlaylists("playlist-id", "playlist-name")) }

        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        val playlists = client.playlists()

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

        val playlists = client.playlists()

        playlists shouldBeRight listOf(
            Playlist(Id("first-id"), Name("first-name")),
            Playlist(Id("second-id"), Name("second-name"))
        )
    }

    @Test
    fun `fails if json is not as expected`() {
        val nextLink = "next-page"
        val http: HttpHandler = {
            when (it.uri.toString()) {
                nextLink -> Response(OK).body("a bad body")
                else -> Response(OK).body(spotifyCurrentUserPlaylists("first-id", "first-name", nextLink))
            }
        }
        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        val playlists = client.playlists()

        playlists.leftOrNull().shouldBeInstanceOf<JsonError>()
    }

    @Test
    fun `token is passed to request`() {
        val http: HttpHandler = { request ->
            request.header("Authorization") shouldBe "Bearer my-token"
            Response(OK).body(spotifyCurrentUserPlaylists("playlist-id", "playlist-name"))
        }

        val client = SpotifyRestClient(http, TestTokenManager("my-token"), "spotify")

        client.playlists()
    }

    @Test
    fun `fails if token manager fails to return a token`() {
        val http: HttpHandler = { Response(OK).body(spotifyCurrentUserPlaylists("playlist-id", "playlist-name")) }
        val client = SpotifyRestClient(http, TestTokenManager(tokenFailure = true), "spotify")

        client.playlists().leftOrNull().shouldBeInstanceOf<HttpResponseError>()
    }

    @Test
    fun `fails if spotify returns a 4xx code`() {
        val http: HttpHandler = { Response(BAD_REQUEST).body("oh dear") }
        val client = SpotifyRestClient(http, TestTokenManager(), "spotify")

        client.playlists() shouldBeLeft HttpResponseError(400, "oh dear")
    }
}