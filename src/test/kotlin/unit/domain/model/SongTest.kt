package unit.domain.model

import io.kotest.matchers.shouldBe
import org.example.domain.model.Artist
import org.example.domain.model.Name
import org.example.domain.model.Song
import kotlin.test.Test

class SongTest {

    private val name = Name("Song Name")
    private val artist = Artist("Artist Name")

    @Test
    fun `same songs are equal`() {
        val song = Song(name, listOf(artist))

        song shouldBe song
    }

    @Test
    fun `songs are the same even if the names are different cases`() {
        val song1 = Song(Name("Song Name"), listOf(artist))
        val song2 = Song(Name("song name"), listOf(artist))

        song1 shouldBe song2
    }

    @Test
    fun `if name is the same and the first artist is the same then the song is the same`() {
        val song1 = Song(name, listOf(artist, Artist("Another Artist")))
        val song2 = Song(name, listOf(artist))

        song1 shouldBe song2
    }

    @Test
    fun `if one artist features in the name of the song and is in the list of artists, then do not consider them when comparing songs`() {
        val withSong = Song(Name("Obey (with YUNGBLUD)"), listOf(Artist("Bring Me The Horizon"), Artist("YUNGBLUD")))
        val featSong = Song(Name("Obey (feat YUNGBLUD)"), listOf(Artist("Bring Me The Horizon"), Artist("YUNGBLUD")))
        val featStopSong = Song(Name("Obey (feat. YUNGBLUD)"), listOf(Artist("Bring Me The Horizon"), Artist("YUNGBLUD")))
        val plainSong = Song(Name("Obey"), listOf(Artist("Bring Me The Horizon")))

        withSong shouldBe plainSong
        featSong shouldBe plainSong
        featStopSong shouldBe plainSong
    }

    @Test
    fun `if name is the same and the any artist is the same then the song is the same`() {
        val song1 = Song(name, listOf(Artist("Another Artist"), artist))
        val song2 = Song(name, listOf(artist))

        song1 shouldBe song2
    }
}