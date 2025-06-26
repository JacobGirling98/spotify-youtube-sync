package unit.domain.model

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import org.example.domain.error.MergeError
import org.example.domain.model.*
import kotlin.test.Test

class SongDictionaryTest {

    private val song = Song(Name("songName"), listOf(Artist("artist")))
    private val id = Id("123")
    private val serviceIds = ServiceIds(Service.SPOTIFY to id)
    private val otherServiceIds = ServiceIds(Service.YOUTUBE_MUSIC to id)

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
                Service.SPOTIFY to id,
                Service.YOUTUBE_MUSIC to id
            )
        )
    }

    @Test
    fun `song name is reported if there was an error merging`() {
        val firstDictionary = SongDictionary(song to serviceIds)
        val secondDictionary = SongDictionary(song to ServiceIds(Service.SPOTIFY to Id("456")))

        firstDictionary.mergeWith(secondDictionary) shouldBeLeft MergeError("Error when combining songName: Ids do not match: 123, 456")
    }
}