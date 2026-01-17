package unit.domain.music

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.example.domain.model.*
import org.example.domain.music.SongMatcher.cleanCoreTitle
import org.example.domain.music.SongMatcher.cleanTitleForCanonicalKey
import org.example.domain.music.SongMatcher.findBestMatch
import kotlin.test.Test

class CleanTitleForCanonicalKeyTest {

    @Test
    fun `basic title remains the same`() {
        cleanTitleForCanonicalKey("my title") shouldBe "my title"
    }

    @Test
    fun `removes common noise patterns`() {
        cleanTitleForCanonicalKey("my title [random]") shouldBe "my title"
        cleanTitleForCanonicalKey("my title (official audio)") shouldBe "my title"
        cleanTitleForCanonicalKey("my title - topic") shouldBe "my title"
        cleanTitleForCanonicalKey("my title lyrics") shouldBe "my title"
        cleanTitleForCanonicalKey("my title official video") shouldBe "my title"
        cleanTitleForCanonicalKey("my title official audio") shouldBe "my title"
        cleanTitleForCanonicalKey("my title mv") shouldBe "my title"
        cleanTitleForCanonicalKey("my title (feat other)") shouldBe "my title"
        cleanTitleForCanonicalKey("my title (with other)") shouldBe "my title"
        cleanTitleForCanonicalKey("my title ft. other") shouldBe "my title"
        cleanTitleForCanonicalKey("my title featuring other") shouldBe "my title"
    }

    @Test
    fun `removes apostrophes`() {
        cleanTitleForCanonicalKey("artist's song") shouldBe "artists song"
    }

    @Test
    fun `replaces intra-word hyphens with space`() {
        cleanTitleForCanonicalKey("my-title") shouldBe "my title"
    }

    @Test
    fun `replaces other special characters except for parenthesis, hyphens and ampersands`() {
        cleanTitleForCanonicalKey("my title _") shouldBe "my title"
        cleanTitleForCanonicalKey("my title ()") shouldBe "my title ()"
        cleanTitleForCanonicalKey("my title -") shouldBe "my title -"
        cleanTitleForCanonicalKey("my title &") shouldBe "my title &"
    }

    @Test
    fun `decodes html entities`() {
        cleanTitleForCanonicalKey("my title &amp;") shouldBe "my title &"
        cleanTitleForCanonicalKey("my title &#38;") shouldBe "my title &"
        cleanTitleForCanonicalKey("my title &#39;") shouldBe "my title" // '
        cleanTitleForCanonicalKey("my title &apos;") shouldBe "my title" // '
        cleanTitleForCanonicalKey("my title &quot;") shouldBe "my title" // "
        cleanTitleForCanonicalKey("my title &#34;") shouldBe "my title" // "
        cleanTitleForCanonicalKey("my title &lt;") shouldBe "my title" // <
        cleanTitleForCanonicalKey("my title &#60;") shouldBe "my title" // <
        cleanTitleForCanonicalKey("my title &gt;") shouldBe "my title" // >
        cleanTitleForCanonicalKey("my title &#62;") shouldBe "my title" // >
    }

    @Test
    fun `trims the title`() {
        cleanTitleForCanonicalKey(" my title ") shouldBe "my title"
    }

    @Test
    fun `lowercases the title`() {
        cleanTitleForCanonicalKey("My Title") shouldBe "my title"
    }
}

class CleanCoreTitleTest {
    @Test
    fun `removes versions from the title`() {
        cleanCoreTitle("my song (remix)") shouldBe "my song"
        cleanCoreTitle("my song - remix") shouldBe "my song"
        cleanCoreTitle("my song (acoustic version)") shouldBe "my song"
        cleanCoreTitle("my song - acoustic") shouldBe "my song"
        cleanCoreTitle("my song (live)") shouldBe "my song"
        cleanCoreTitle("my song - live") shouldBe "my song"
        cleanCoreTitle("my song (taylor s version)") shouldBe "my song"
        cleanCoreTitle("my song - taylor s version") shouldBe "my song"
        cleanCoreTitle("my song (atl s version)") shouldBe "my song"
        cleanCoreTitle("my song - atl s version") shouldBe "my song"
        cleanCoreTitle("my song (from the room below)") shouldBe "my song"
        cleanCoreTitle("my song - from the room below") shouldBe "my song"
    }
}


class SongMatcherTest {

    private val artist = Artist("Bring Me The Horizon")
    private val song = Song(Name("Obey"), listOf(artist))

    @Test
    fun `matches exact title and channel name`() {
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Obey", "Bring Me The Horizon")
        )

        val match = findBestMatch(song, candidates)
        match.shouldNotBeNull()
        match.id shouldBe Id("1")
    }

    @Test
    fun `matches normalized title with (feat)`() {
        val complexSong = Song(Name("Obey (feat. YUNGBLUD)"), listOf(artist))
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Obey", "Bring Me The Horizon")
        )

        val match = findBestMatch(complexSong, candidates)
        match.shouldNotBeNull()
        match.id shouldBe Id("1")
    }

    @Test
    fun `matches normalized candidate title with Official Video tag`() {
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Obey [Official Video]", "Bring Me The Horizon")
        )

        val match = findBestMatch(song, candidates)
        match.shouldNotBeNull()
        match.id shouldBe Id("1")
    }

    @Test
    fun `matches if artist is in the candidate title but not channel`() {
        // Sometimes YouTube channels are VEVO or Topic, but the title has the artist
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Bring Me The Horizon - Obey", "BMTHVEVO")
        )

        val match = findBestMatch(song, candidates)
        match.shouldNotBeNull()
        match.id shouldBe Id("1")
    }

    @Test
    fun `matches case insensitive`() {
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "obey", "bring me the horizon")
        )

        val match = findBestMatch(song, candidates)
        match.shouldNotBeNull()
        match.id shouldBe Id("1")
    }

    @Test
    fun `does not match if title matches but artist is completely missing`() {
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Obey", "Random Vlogger")
        )

        val match = findBestMatch(song, candidates)
        match shouldBe null
    }

    @Test
    fun `does not match if title is completely different`() {
        val candidates = listOf(
            SongMatchCandidate(Id("1"), "Throne", "Bring Me The Horizon")
        )

        val match = findBestMatch(song, candidates)
        match shouldBe null
    }

    @Test
    fun `real world - matches with and without apostrophe`() {
        val spotifySong = Song(Name("It's Only Smiles"), listOf(Artist("Periphery")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Its Only Smiles", "Periphery")

        val match = findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }

    @Test
    fun `real world - matches with different formatting and artist lists`() {
        val spotifySong = Song(Name("ANYTHING > HUMAN"), listOf(Artist("Bad Omens"), Artist("ERRA")))
        val ytCandidate = SongMatchCandidate(Id("1"), "ANYTHING ᐳ HUMAN", "Bad Omens")

        val match = findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }

    @Test
    fun `real world - matches acoustic version with different syntax`() {
        val spotifySong = Song(Name("Bad Life - acoustic"), listOf(Artist("Sigrid")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Bad Life (acoustic)", "Sigrid")

        val match = findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }
    
    @Test
    fun `real world - matches different version tags (ATL's Version)`() {
        val spotifySong = Song(Name("Dear Maria, Count Me In - ATL's Version"), listOf(Artist("All Time Low")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Dear Maria, Count Me In (ATL's Version)", "All Time Low")

        val match = findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }
    
    @Test
    fun `real world - matches different version tags (Taylor's Version)`() {
        val spotifySong = Song(Name("Love Story (Taylor’s Version)"), listOf(Artist("Taylor Swift")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Love Story - Taylor’s Version", "Taylor Swift")

        val match = findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }

    @Test
    fun `real world - matches remixes or not`() {
        val spotifySong = Song(Name("Save Your Tears (with Ariana Grande) (Remix)"), listOf(Artist("The Weeknd"), Artist("Ariana Grande")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Save Your Tears (Remix)", "The Weeknd")

        val match = findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
    }

    @Test
    fun `real world - does not match if version tags are different`() {
        val spotifySong = Song(Name("Bad Life"), listOf(Artist("Sigrid")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Bad Life (acoustic)", "Sigrid")

        val match = findBestMatch(spotifySong, listOf(ytCandidate))
        match shouldBe null
    }

    @Test
    fun `real world - does not match if one has remix tag and other does not`() {
        val spotifySong = Song(Name("Save Your Tears"), listOf(Artist("The Weeknd")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Save Your Tears (Remix)", "The Weeknd")

        val match = findBestMatch(spotifySong, listOf(ytCandidate))
        match shouldBe null
    }

    @Test
    fun `real world - matches when source has multiple artists and target has one`() {
        val spotifySong = Song(Name("60cm of Steel"), listOf(Artist("Alpha Wolf"), Artist("Holding Absence")))
        // YouTube Music often lists the main artist in the channel/artist field, and sometimes misses features or lists them differently
        val ytCandidate = SongMatchCandidate(Id("1"), "60cm of Steel", "Alpha Wolf")

        val match = findBestMatch(spotifySong, listOf(ytCandidate))
        match.shouldNotBeNull()
        match.id shouldBe Id("1")
    }

    @Test
    fun `real world - covers do not match`() {
        val spotifySong = Song(Name("Wonderwall - Spotify Singles"), listOf(Artist("Bring Me The Horizon")))
        val ytCandidate = SongMatchCandidate(Id("1"), "Wonderwalls", "Oasis")

        val match = findBestMatch(spotifySong, listOf(ytCandidate))
        match shouldBe null
    }

    @Test
    fun `real world - weird characters`() {
        val spotifySong = Song(Name("Can't Turn Back"), listOf(Artist("Currents")))
        val otherSpotifySong = Song(Name("Cigarettes & Sabotage"), listOf(Artist("All Time Low")))

        val ytCandidate = SongMatchCandidate(Id("1"), "Can&#39;t Turn Back", "Currents - topic")
        val otherYtCandidate = SongMatchCandidate(Id("2"), "Cigarettes &amp; Sabotage", "All Time Low - Topic")

        findBestMatch(spotifySong, listOf(ytCandidate))?.id shouldBe Id("1")
        findBestMatch(otherSpotifySong, listOf(otherYtCandidate))?.id shouldBe Id("2")
    }
}

