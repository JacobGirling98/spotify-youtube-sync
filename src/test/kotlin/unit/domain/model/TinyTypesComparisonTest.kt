package org.example.unit.domain.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.example.domain.model.Artist
import org.example.domain.model.Name
import org.example.domain.model.Song
import kotlin.test.Test

class TinyTypesComparisonTest {

    @Test
    fun `Name comparison should be case insensitive`() {
        Name("Test") shouldBe Name("test")
        Name("TEST") shouldBe Name("test")
        Name("Test").hashCode() shouldBe Name("test").hashCode()
    }

    @Test
    fun `Artist comparison should be case insensitive`() {
        Artist("Artist") shouldBe Artist("artist")
        Artist("ARTIST") shouldBe Artist("artist")
        Artist("Artist").hashCode() shouldBe Artist("artist").hashCode()
    }

    @Test
    fun `Song comparison should be case insensitive`() {
        val song1 = Song(Name("Song"), listOf(Artist("Artist")))
        val song2 = Song(Name("song"), listOf(Artist("artist")))

        song1 shouldBe song2
        song1.hashCode() shouldBe song2.hashCode()
    }
}
