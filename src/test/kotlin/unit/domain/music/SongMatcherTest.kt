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

    @Test
    fun `real world - matches with and without apostrophe`() {
        val spotifySong = Song(Name("It's Only Smiles"), listOf(Artist("Periphery")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Its Only Smiles", "Periphery")

        val match = SongMatcher.findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }

    @Test
    fun `real world - matches with different formatting and artist lists`() {
        val spotifySong = Song(Name("ANYTHING > HUMAN"), listOf(Artist("Bad Omens"), Artist("ERRA")))
        val ytCandidate = SongMatchCandidate(Id("1"), "ANYTHING ᐳ HUMAN", "Bad Omens")
        
        val match = SongMatcher.findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }

    @Test
    fun `real world - matches acoustic version with different syntax`() {
        val spotifySong = Song(Name("Bad Life - acoustic"), listOf(Artist("Sigrid")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Bad Life (acoustic)", "Sigrid")

        val match = SongMatcher.findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }
    
    @Test
    fun `real world - matches different version tags (ATL's Version)`() {
        val spotifySong = Song(Name("Dear Maria, Count Me In - ATL's Version"), listOf(Artist("All Time Low")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Dear Maria, Count Me In (ATL's Version)", "All Time Low")

        val match = SongMatcher.findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }
    
    @Test
    fun `real world - matches different version tags (Taylor's Version)`() {
        val spotifySong = Song(Name("Love Story (Taylor’s Version)"), listOf(Artist("Taylor Swift")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Love Story - Taylor’s Version", "Taylor Swift")

        val match = SongMatcher.findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }

    @Test
    fun `real world - matches remixes or not`() {
        val spotifySong = Song(Name("Save Your Tears (with Ariana Grande) (Remix)"), listOf(Artist("The Weeknd"), Artist("Ariana Grande")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Save Your Tears (Remix)", "The Weeknd")

        val match = SongMatcher.findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }

    @Test
    fun `real world - does not match if version tags are different`() {
        val spotifySong = Song(Name("Bad Life"), listOf(Artist("Sigrid")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Bad Life (acoustic)", "Sigrid")

        val match = SongMatcher.findBestMatch(spotifySong, listOf(ytCandidate))
        match shouldBe null
    }

    @Test
    fun `real world - does not match if one has remix tag and other does not`() {
        val spotifySong = Song(Name("Save Your Tears"), listOf(Artist("The Weeknd")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Save Your Tears (Remix)", "The Weeknd")

        val match = SongMatcher.findBestMatch(spotifySong, listOf(ytCandidate))
        match shouldBe null
    }
}

