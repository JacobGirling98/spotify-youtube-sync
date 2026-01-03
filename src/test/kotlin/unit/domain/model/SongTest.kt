package unit.domain.model

import io.kotest.matchers.shouldBe
import org.example.domain.model.Artist
import org.example.domain.model.CanonicalSongKey
import org.example.domain.model.Name
import org.example.domain.model.Song
import kotlin.test.Test

class SongTest {

    private val name = Name("Song Name")
    private val artist = Artist("Artist Name")

    @Test
    fun `toCanonicalKey handles empty artist list`() {
        val song = Song(Name("Instrumental Song"), emptyList())
        song.toCanonicalKey() shouldBe CanonicalSongKey("instrumental song::")
    }

    @Test
    fun `toCanonicalKey normalizes title cases and special characters`() {
        val song = Song(Name("SONG-NAME (Official Video)"), listOf(Artist("ARTIST NAME")))
        song.toCanonicalKey() shouldBe CanonicalSongKey("song name::artist name")
    }

    @Test
    fun `toCanonicalKey works for basic case`() {
        val song = Song(Name("Song Name"), listOf(artist))
        song.toCanonicalKey() shouldBe CanonicalSongKey("song name::artist name")
    }

    @Test
    fun `toCanonicalKey works with 'with' cases`() {
        val song = Song(Name("Song Name (with Guest)"), listOf(artist))
        song.toCanonicalKey() shouldBe CanonicalSongKey("song name::artist name")
    }

    @Test
    fun `toCanonicalKey works with 'featured' and 'featuring'`() {
        val song = Song(Name("Song Name ft. Guest"), listOf(artist))
        song.toCanonicalKey() shouldBe CanonicalSongKey("song name::artist name")

        val song2 = Song(Name("Song Name featuring Guest"), listOf(artist))
        song2.toCanonicalKey() shouldBe CanonicalSongKey("song name::artist name")
    }

    @Test
    fun `toCanonicalKey leaves in 'remix'`() {
        val song = Song(Name("Song Name (Remix)"), listOf(artist))
        song.toCanonicalKey() shouldBe CanonicalSongKey("song name (remix)::artist name")
    }

    @Test
    fun `toCanonicalKey leaves in 'version'`() {
        val acousticSong = Song(Name("Song Name (Acoustic Version)"), listOf(artist))
        acousticSong.toCanonicalKey() shouldBe CanonicalSongKey("song name (acoustic version)::artist name")

        val liveSong = Song(Name("Song Name - Live Version"), listOf(artist))
        liveSong.toCanonicalKey() shouldBe CanonicalSongKey("song name - live version::artist name")

        val newVersion = Song(Name("Song Name (ATL Version)"), listOf(artist))
        newVersion.toCanonicalKey() shouldBe CanonicalSongKey("song name (atl version)::artist name")

        val anotherNewVersion = Song(Name("Song Name - Radio Version"), listOf(artist))
        anotherNewVersion.toCanonicalKey() shouldBe CanonicalSongKey("song name - radio version::artist name")
    }

    @Test
    fun `toCanonicalKey preserves ampersand`() {
        val song = Song(Name("Rise & Fall"), listOf(Artist("Currents")))
        song.toCanonicalKey() shouldBe CanonicalSongKey("rise & fall::currents")
    }
}