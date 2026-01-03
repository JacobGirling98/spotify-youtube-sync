package unit.domain.model

import io.kotest.matchers.shouldBe
import org.example.domain.model.*
import kotlin.test.Test

class PlaylistTest {
    val songA = Song(Name("Song A"), listOf(Artist("Artist A")))
    val songB = Song(Name("Song B"), listOf(Artist("Artist B")))


    @Test
    fun `deltaWith returns an empty Delta when the playlists are the same`() {
        val sourcePlaylist = Playlist(
            Id("SP1"),
            Name("My Playlist"),
            SongDictionary(
                songA to ServiceIds(Service.SPOTIFY to Id("s1")),
            )
        )
        val targetPlaylist = Playlist(
            Id("YTP1"),
            Name("My Playlist"),
            SongDictionary(
                songA to ServiceIds(Service.YOUTUBE_MUSIC to Id("yt1")),
            )
        )

        sourcePlaylist.deltaWith(targetPlaylist) shouldBe Delta.empty()
    }

    @Test
    fun `deltaWith gives songs that are missing in the other playlist`() {
        val sourcePlaylist = Playlist(
            Id("SP1"),
            Name("My Playlist"),
            SongDictionary(
                songA to ServiceIds(Service.SPOTIFY to Id("s1")),
                songB to ServiceIds(Service.SPOTIFY to Id("s2")),
            )
        )
        val targetPlaylist = Playlist(
            Id("YTP1"),
            Name("My Playlist"),
            SongDictionary(
                songA to ServiceIds(Service.YOUTUBE_MUSIC to Id("yt1")),
            )
        )

        sourcePlaylist.deltaWith(targetPlaylist) shouldBe Delta(removed = listOf(songB))
    }

    @Test
    fun `deltaWith gives songs that are added in the other playlist`() {
        val sourcePlaylist = Playlist(
            Id("SP1"),
            Name("My Playlist"),
            SongDictionary(
                songA to ServiceIds(Service.SPOTIFY to Id("s1")),
            )
        )
        val targetPlaylist = Playlist(
            Id("YTP1"),
            Name("My Playlist"),
            SongDictionary(
                songA to ServiceIds(Service.YOUTUBE_MUSIC to Id("yt1")),
                songB to ServiceIds(Service.YOUTUBE_MUSIC to Id("yt2")),
            )
        )

        sourcePlaylist.deltaWith(targetPlaylist) shouldBe Delta(added = listOf(songB))
    }
    
    @Test
    fun `deltaWith handles both added and missing songs in the other playlist`() {
        val sourcePlaylist = Playlist(
            Id("SP1"),
            Name("My Playlist"),
            SongDictionary(
                songA to ServiceIds(Service.SPOTIFY to Id("s1")),
            )
        )
        val targetPlaylist = Playlist(
            Id("YTP1"),
            Name("My Playlist"),
            SongDictionary(
                songB to ServiceIds(Service.YOUTUBE_MUSIC to Id("yt2")),
            )
        )

        sourcePlaylist.deltaWith(targetPlaylist) shouldBe Delta(
            added = listOf(songB),
            removed = listOf(songA)
        )
    }

    @Test
    fun `deltaWith returns empty Delta when songs match fuzzy`() {
        val spotifySong = Song(Name("60cm of Steel"), listOf(Artist("Alpha Wolf"), Artist("Holding Absence")))
        val ytSong = Song(Name("60cm of Steel"), listOf(Artist("Alpha Wolf")))

        val sourcePlaylist = Playlist(
            Id("SP1"),
            Name("My Playlist"),
            SongDictionary(
                spotifySong to ServiceIds(Service.SPOTIFY to Id("s1"))
            )
        )
        val targetPlaylist = Playlist(
            Id("YTP1"),
            Name("My Playlist"),
            SongDictionary(
                ytSong to ServiceIds(Service.YOUTUBE_MUSIC to Id("yt1"))
            )
        )

        // Even though canonical keys are different, they should fuzzy match
        sourcePlaylist.deltaWith(targetPlaylist) shouldBe Delta.empty()
    }
}