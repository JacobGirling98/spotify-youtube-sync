package unit.domain.model

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.example.domain.model.*
import kotlin.test.Test

class SongDictionaryTest {

    private val song = Song(Name("songName"), listOf(Artist("artist")))
    private val id = Id("123")
    private val serviceIds = ServiceIds(Service.SPOTIFY to id)
    private val otherServiceIds = ServiceIds(Service.YOUTUBE_MUSIC to id)

    @Test
    fun `can construct via vararg of pairs`() {
        val entry = SongEntry(song, serviceIds)
        val expected = SongDictionary(mapOf(song.toCanonicalKey() to entry))
        
        SongDictionary(song to serviceIds) shouldBe expected
    }

    @Test
    fun `can merge two dictionaries when songs are different`() {
        val firstDictionary = SongDictionary(song to serviceIds)
        val otherSong = song.copy(Name("Other song"))
        val secondDictionary = SongDictionary(otherSong to serviceIds)

        firstDictionary.mergeWith(secondDictionary) shouldBeRight SongDictionary(
            song to serviceIds,
            otherSong to serviceIds
        )
    }

    @Test
    fun `can merge two dictionaries if second is empty`() {
        val firstDictionary = SongDictionary(song to serviceIds)
        val secondDictionary = SongDictionary()

        firstDictionary.mergeWith(secondDictionary) shouldBeRight SongDictionary(song to serviceIds)
    }

    @Test
    fun `can merge two dictionaries if first is empty`() {
        val secondDictionary = SongDictionary(song to serviceIds)
        val firstDictionary = SongDictionary()

        firstDictionary.mergeWith(secondDictionary) shouldBeRight SongDictionary(song to serviceIds)
    }

    @Test
    fun `song is not duplicated if it exists in second dictionary with same service and id`() {
        val firstDictionary = SongDictionary(song to serviceIds)
        val secondDictionary = SongDictionary(song to serviceIds)

        firstDictionary.mergeWith(secondDictionary) shouldBeRight SongDictionary(song to serviceIds)
    }

    @Test
    fun `song with multiple services is combined`() {
        val firstDictionary = SongDictionary(song to serviceIds)
        val secondDictionary = SongDictionary(song to otherServiceIds)

        firstDictionary.mergeWith(secondDictionary) shouldBeRight SongDictionary(
            song to ServiceIds(
                Service.SPOTIFY to id,
                Service.YOUTUBE_MUSIC to id
            )
        )
    }

    @Test
    fun `ids returns service ids for a song`() {
        val dictionary = SongDictionary(song to serviceIds)

        dictionary.ids(song) shouldBe serviceIds
    }

    @Test
    fun `ids returns null if song is not in dictionary`() {
        val dictionary = SongDictionary(song to serviceIds)
        val otherSong = song.copy(Name("Other song"))

        dictionary.ids(otherSong) shouldBe null
    }

    @Test
    fun `merges entry with fuzzy matching key and preserves original song data`() {
        val spotifySong = Song(Name("60cm of Steel"), listOf(Artist("Alpha Wolf"), Artist("Holding Absence")))
        val ytSong = Song(Name("60cm of Steel"), listOf(Artist("Alpha Wolf"))) // Missing second artist

        val spotifyEntry = SongEntry(spotifySong, ServiceIds(Service.SPOTIFY to Id("s1")))
        val ytEntry = SongEntry(ytSong, ServiceIds(Service.YOUTUBE_MUSIC to Id("y1")))

        val dictionary = SongDictionary(mapOf(spotifySong.toCanonicalKey() to spotifyEntry))
        val otherDictionary = SongDictionary(mapOf(ytSong.toCanonicalKey() to ytEntry))

        // Pre-condition: Keys are different
        spotifySong.toCanonicalKey() shouldNotBe ytSong.toCanonicalKey()

        val merged = dictionary.mergeWith(otherDictionary).shouldBeRight()

        merged.entries.size shouldBe 1
        val entry = merged.entries.values.first()
        
        // Should preserve the original (Spotify) song data
        entry.song shouldBe spotifySong
        // Should have both IDs
        entry.serviceIds shouldBe ServiceIds(
            Service.SPOTIFY to Id("s1"),
            Service.YOUTUBE_MUSIC to Id("y1")
        )
    }

    @Test
    fun `does not fuzzy merge if songs are distinct variants`() {
        val original = Song(Name("Song A"), listOf(Artist("Artist")))
        val live = Song(Name("Song A (Live)"), listOf(Artist("Artist")))

        val dictionary = SongDictionary(original to ServiceIds(Service.SPOTIFY to Id("1")))
        val otherDictionary = SongDictionary(live to ServiceIds(Service.YOUTUBE_MUSIC to Id("2")))

        val merged = dictionary.mergeWith(otherDictionary).shouldBeRight()

        merged.entries.size shouldBe 2
    }
}