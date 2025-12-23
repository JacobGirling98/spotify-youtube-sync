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

        song.equals(song) shouldBe true
    }

    @Test
    fun `songs are the same even if the names are different cases`() {
        val song1 = Song(Name("Song Name"), listOf(artist))
        val song2 = Song(Name("song name"), listOf(artist))

        song1.equals(song2) shouldBe true
    }
}