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
    private val thirdId = Id("3")

    private val firstSong = song("first song")
    private val secondSong = song("second song")
    private val thirdSong = song("third song")

    private val playlists = listOf(
        Playlist(
            Id("first playlist"),
            Name("first playlist"),
            SongDictionary(firstSong to ServiceIds(Service.SPOTIFY to firstId))
        ),
        Playlist(
            Id("second playlist"),
            Name("second playlist"),
            SongDictionary(
                firstSong to ServiceIds(Service.SPOTIFY to firstId),
                secondSong to ServiceIds(Service.SPOTIFY to secondId)
            )
        )
    )

    private val allSongs = SongDictionary(
        firstSong to ServiceIds(Service.SPOTIFY to firstId),
        secondSong to ServiceIds(Service.SPOTIFY to secondId),
        thirdSong to ServiceIds(Service.SPOTIFY to thirdId)
    )

    private val service = FakeMusicService(Service.SPOTIFY, playlists, allSongs)

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
        metadata.map { it.name } shouldBe listOf(Name("first playlist"), Name("second playlist"))
    }

    @Test
    fun `can get playlists from metadata`() {
        val metadata = listOf(PlaylistMetadata(Id("first playlist"), Name("first playlist")))
        val result = service.playlists(metadata).shouldBeRight()

        result.size shouldBe 1
        result.first().id shouldBe Id("first playlist")
        result.first().name shouldBe Name("first playlist")
        result.first().songs.entries.size shouldBe 1
        result.first().songs.entries.keys.first() shouldBe firstSong
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
    fun `can add a song to a playlist`() {
        // Act: Add second song to the first playlist
        service.addSongToPlaylist(secondId, Id("first playlist")).shouldBeRight()

        // Assert: Tracks should now contain 2 songs
        val tracks = service.tracks(Id("first playlist")).shouldBeRight()
        tracks.entries.size shouldBe 2

        // Assert: Playlist should now contain 2 songs
        val playlist = service.playlists().shouldBeRight().find { it.name == Name("first playlist") }
        playlist?.songs?.entries?.size shouldBe 2

        // Verify the second song is indeed in the playlist
        val secondSongEntry = tracks.entries.keys.find { it == secondSong }
        secondSongEntry shouldBe secondSong
    }

    @Test
    fun `can add a song to a playlist that isn't in any other playlists`() {
        // Act: Add second song to the first playlist
        service.addSongToPlaylist(thirdId, Id("first playlist")).shouldBeRight()

        // Assert: Tracks should now contain 2 songs
        val tracks = service.tracks(Id("first playlist")).shouldBeRight()
        tracks.entries.size shouldBe 2

        // Assert: Playlist should now contain 2 songs
        val playlist = service.playlists().shouldBeRight().find { it.name == Name("first playlist") }
        playlist?.songs?.entries?.size shouldBe 2

        // Verify the second song is indeed in the playlist
        val newSong = tracks.entries.keys.find { it == thirdSong }
        newSong shouldBe thirdSong
    }

    @Test
    fun `can delete a song from a playlist`() {
        // Act: Delete the first song from the first playlist
        service.deleteSongFromPlaylist(firstId, Id("first playlist")).shouldBeRight()

        // Assert: Tracks should now be empty
        val tracks = service.tracks(Id("first playlist")).shouldBeRight()
        tracks.entries.size shouldBe 0
    }

    @Test
    fun `deleteSongFromPlaylist returns error for unknown playlist`() {
        service.deleteSongFromPlaylist(firstId, Id("unknown")).shouldBeLeft()
    }

    @Test
    fun `deleteSongFromPlaylist returns error for unknown song`() {
        service.deleteSongFromPlaylist(Id("unknown"), Id("first playlist")).shouldBeLeft()
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