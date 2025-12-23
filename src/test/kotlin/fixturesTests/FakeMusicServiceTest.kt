package fixturesTests

import fixtures.FakeMusicService
import fixtures.data.song
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import org.example.domain.model.*
import kotlin.test.Test

class FakeMusicServiceTest {

    private val firstId = Id("1")
    private val secondId = Id("2")

    private val firstSong = song("first song")
    private val secondSong = song("second song")

    private val playlists = listOf(
        Playlist(
            Id("first playlist"),
            Name("first playlist"),
            SongDictionary(firstSong to ServiceIds(Service.SPOTIFY to firstId))
        ),
        Playlist(
            Id("all songs"),
            Name("all songs"),
            SongDictionary(
                firstSong to ServiceIds(Service.SPOTIFY to firstId),
                secondSong to ServiceIds(Service.SPOTIFY to secondId)
            )
        )
    )

    private val service = FakeMusicService(Service.SPOTIFY, playlists)

    @Test
    fun `just returns the given playlists`() {
        val playlists = service.playlists().shouldBeRight()
        val playlist = playlists.find { it.name == Name("first playlist") }!!
        playlist.songs.entries.size shouldBe 1
    }

    @Test
    fun `can get playlist metadata`() {
        val metadata = service.playlistMetadata().shouldBeRight()
        metadata.size shouldBe 2
        metadata.map { it.name } shouldBe listOf(Name("first playlist"), Name("all songs"))
    }

    @Test
    fun `can get playlists from metadata`() {
        val metadata = listOf(org.example.domain.model.PlaylistMetadata(Id("custom-id"), Name("Custom Playlist")))
        val result = service.playlists(metadata).shouldBeRight()
        
        result.size shouldBe 1
        result.first().id shouldBe Id("custom-id")
        result.first().name shouldBe Name("Custom Playlist")
        result.first().songs.entries.isEmpty() shouldBe true
    }

    @Test
    fun `can search for a song`() {
        val result = service.search(firstSong).shouldBeRight()
        result.entries.size shouldBe 1
        result.entries[firstSong] shouldBe org.example.domain.model.ServiceIds(Service.SPOTIFY to firstId)
    }

    @Test
    fun `search returns error for unknown song`() {
        val unknownSong = song("unknown")
        service.search(unknownSong).shouldBeLeft()
    }

    @Test
    fun `tracks returns error for unknown playlist`() {
        service.tracks(Id("unknown")).shouldBeLeft()
    }

    @Test
    fun `addSongToPlaylist returns error for unknown playlist`() {
        service.addSongToPlaylist(firstId, Id("unknown")).shouldBeLeft()
    }

    @Test
    fun `addSongToPlaylist returns error for unknown song`() {
        service.addSongToPlaylist(Id("unknown"), Id("first playlist")).shouldBeLeft()
    }
}