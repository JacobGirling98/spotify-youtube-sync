package unit.http.auth

import io.kotest.matchers.shouldBe
import org.example.http.auth.spotifyAuthRequestUrl
import kotlin.test.Test

class SpotifyAuthRequestUrlTest {
    @Test
    fun `should return an encoded URL`() {
        spotifyAuthRequestUrl("client-id") shouldBe "https://accounts.spotify.com/authorize?response_type=code&client_id=client-id&scope=playlist-read-private+playlist-read-collaborative&redirect_uri=http://127.0.0.1:8000/spotify_callback"
    }
}
