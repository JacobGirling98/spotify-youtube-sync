package unit.domain

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import org.example.domain.model.*
import org.example.domain.model.Service.SPOTIFY
import org.example.domain.model.Service.YOUTUBE_MUSIC
import kotlin.test.Test

class SongDictionaryTest {

    private val song = Song(Name("songName"), listOf(Artist("artist")))
    private val id = Id("123")
    private val serviceIds = ServiceIds(SPOTIFY to id)
    private val otherServiceIds = ServiceIds(YOUTUBE_MUSIC to id)

    @Test
    fun `can construct via vararg of pairs`() {
        SongDictionary(mapOf(song to serviceIds)) shouldBe SongDictionary(song to serviceIds)
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
                SPOTIFY to id,
                YOUTUBE_MUSIC to id
            )
        )
    }
}