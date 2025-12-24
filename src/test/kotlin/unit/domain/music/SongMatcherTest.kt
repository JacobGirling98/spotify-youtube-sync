package unit.domain.music

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.example.domain.model.*
import org.example.domain.music.SongMatcher
import kotlin.test.Test

class SongMatcherTest {

    private val artist = Artist("Bring Me The Horizon")
    private val song = Song(Name("Obey"), listOf(artist))

    @Test
    fun `matches exact title and channel name`() {
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Obey", "Bring Me The Horizon")
        )

        val match = SongMatcher.findBestMatch(song, candidates)
        match.shouldNotBeNull()
        match.id shouldBe Id("1")
    }

    @Test
    fun `matches normalized title with (feat)`() {
        val complexSong = Song(Name("Obey (feat. YUNGBLUD)"), listOf(artist))
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Obey", "Bring Me The Horizon")
        )

        val match = SongMatcher.findBestMatch(complexSong, candidates)
        match.shouldNotBeNull()
        match.id shouldBe Id("1")
    }

    @Test
    fun `matches normalized candidate title with Official Video tag`() {
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Obey [Official Video]", "Bring Me The Horizon")
        )

        val match = SongMatcher.findBestMatch(song, candidates)
        match.shouldNotBeNull()
        match.id shouldBe Id("1")
    }

    @Test
    fun `matches if artist is in the candidate title but not channel`() {
        // Sometimes YouTube channels are VEVO or Topic, but the title has the artist
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Bring Me The Horizon - Obey", "BMTHVEVO")
        )

        val match = SongMatcher.findBestMatch(song, candidates)
        match.shouldNotBeNull()
        match.id shouldBe Id("1")
    }

    @Test
    fun `matches case insensitive`() {
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "obey", "bring me the horizon")
        )

        val match = SongMatcher.findBestMatch(song, candidates)
        match.shouldNotBeNull()
        match.id shouldBe Id("1")
    }

    @Test
    fun `does not match if title matches but artist is completely missing`() {
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Obey", "Random Vlogger")
        )

        val match = SongMatcher.findBestMatch(song, candidates)
        match shouldBe null
    }

    @Test
    fun `does not match if title is completely different`() {
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Throne", "Bring Me The Horizon")
        )

        val match = SongMatcher.findBestMatch(song, candidates)
        match shouldBe null
    }
}
