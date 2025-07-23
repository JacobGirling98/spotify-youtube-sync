package unit.http.youtube

import fixtures.TestTokenManager
import fixtures.youTubeCurrentUserPlaylists
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.example.domain.error.HttpResponseError
import org.example.domain.error.JsonError
import org.example.domain.model.Id
import org.example.domain.model.Name
import org.example.http.youtube.client.YouTubeRestClient
import org.example.http.youtube.model.Playlist
import org.example.http.youtube.model.PlaylistSnippet
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import kotlin.test.Test

class YouTubeRestClientTest {

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
}