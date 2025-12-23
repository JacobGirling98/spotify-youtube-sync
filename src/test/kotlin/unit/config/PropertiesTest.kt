package unit.config

import arrow.core.Either
import arrow.core.Either.Right
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import org.example.config.ConfigError
import org.example.config.loadProperties
import org.example.domain.model.Name
import java.util.*
import kotlin.test.Test

class PropertiesTest {

    @Test
    fun `successfully loads all properties when all are present`() {
        loadProperties(readProps(validProps())) shouldBeRight org.example.config.Properties(
            playlists = listOf(Name("MyPlaylist")),
            redirectServerUri = "http://localhost",
            redirectServerPort = 8080,
            spotifyBaseUrl = "https://api.spotify.com",
            youtubeBaseUrl = "https://www.googleapis.com"
        )
    }

    @Test
    fun `returns properties with multiple playlists`() {
        val props = validProps().apply { setProperty("playlists", "MyPlaylist,AnotherPlaylist,ThirdPlaylist") }

        loadProperties(readProps(props)).shouldBeRight().playlists shouldBe listOf(
            Name("MyPlaylist"),
            Name("AnotherPlaylist"),
            Name("ThirdPlaylist")
        )

    }

    @Test
    fun `returns error if PLAYLISTS is missing`() {
        val props = validProps().apply { remove("playlists") }

        loadProperties(readProps(props)) shouldBeLeft ConfigError.PlaylistsNotSet
    }

    @Test
    fun `returns error if REDIRECT_URI is missing`() {
        val props = validProps().apply { remove("redirect_uri") }

        loadProperties(readProps(props)) shouldBeLeft ConfigError.RedirectUriNotSet
    }

    @Test
    fun `returns error if REDIRECT_PORT is missing`() {
        val props = validProps().apply { remove("redirect_port") }

        loadProperties(readProps(props)) shouldBeLeft ConfigError.RedirectPortNotSet
    }

    @Test
    fun `returns error if REDIRECT_PORT is not a number`() {
        val props = validProps().apply { setProperty("redirect_port", "not-a-number") }

        loadProperties(readProps(props)) shouldBeLeft ConfigError.RedirectPortNotSet
    }

    @Test
    fun `returns error if SPOTIFY_BASE_URL is missing`() {
        val props = validProps().apply { remove("spotify_base_url") }

        loadProperties(readProps(props)) shouldBeLeft ConfigError.SpotifyBaseUrlNotSet
    }

    @Test
    fun `returns error if YOUTUBE_BASE_URL is missing`() {
        val props = validProps().apply { remove("youtube_base_url") }

        loadProperties(readProps(props)) shouldBeLeft ConfigError.YouTubeBaseUrlNotSet
    }

    private fun readProps(props: Properties): (String) -> Either<ConfigError, Properties> =
        { _: String -> Right(props) }

    private fun validProps() = Properties().apply {
        setProperty("playlists", "MyPlaylist")
        setProperty("redirect_uri", "http://localhost")
        setProperty("redirect_port", "8080")
        setProperty("spotify_base_url", "https://api.spotify.com")
        setProperty("youtube_base_url", "https://www.googleapis.com")
    }
}