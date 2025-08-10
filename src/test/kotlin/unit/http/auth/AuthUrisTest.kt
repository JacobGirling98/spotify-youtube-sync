package unit.http.auth

import io.kotest.matchers.shouldBe
import org.example.http.auth.SpotifyAuth
import org.example.http.auth.YouTubeAuth
import kotlin.test.Test

class AuthUrisTest {

    private val spotifyAuth = SpotifyAuth("local-server")
    private val youTubeAuth = YouTubeAuth("local-server")

    @Test
    fun `should return a spotify encoded URL`() {
        spotifyAuth.codeUri("client-id") shouldBe "https://accounts.spotify.com/authorize?response_type=code&client_id=client-id&scope=playlist-read-private+playlist-read-collaborative&redirect_uri=local-server/spotify_callback"
    }

    @Test
    fun `should return an youtube URL`() {
        youTubeAuth.codeUri("client-id") shouldBe "https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=client-id&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fyoutube&redirect_uri=local-server/youtube_callback&access_type=offline&prompt=consent"
    }
}