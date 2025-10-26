package unit.domain.music

import arrow.core.Either
import fixtures.data.*
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import org.example.domain.error.NoResultsError
import org.example.domain.model.*
import org.example.domain.model.Service.SPOTIFY
import org.example.domain.model.Service.YOUTUBE_MUSIC
import org.example.domain.music.MusicService
import org.example.domain.music.createDictionary
import org.example.domain.music.fillDictionary
import org.example.domain.music.subsetOf
import kotlin.test.Test
import kotlin.test.fail

class DictionaryTest {
    private val song = song("My Song")
    private val otherSong = song("Other Song")
    private val spotifyId = Id("123")
    private val otherId = Id("456")
    private val youtubeId = Id("321")
    private val spotifyServiceId = spotifyServiceId(id = spotifyId)
    private val otherServiceId = spotifyServiceId(id = otherId)
    private val youtubeServiceId = youtubeServiceId(id = youtubeId)

    @Test
    fun `can create a dictionary from a single playlist`() {
        val playlists = listOf(playlist("My playlist", songDictionary(song, spotifyServiceId)))

        val dictionary = createDictionary(playlists)

        dictionary shouldBeRight SongDictionary(song to spotifyServiceId)
    }

    @Test
    fun `can create a dictionary from multiple playlists with duplicate songs`() {
        val playlists = listOf(
            playlist("My playlist", songDictionary(song, spotifyServiceId)),
            playlist("My second playlist", songDictionary(otherSong, otherServiceId)),
            playlist("My youtube playlist", songDictionary(song, youtubeServiceId)),
        )

        val dictionary = createDictionary(playlists)

        dictionary shouldBeRight SongDictionary(
            song to ServiceIds(SPOTIFY to spotifyId, YOUTUBE_MUSIC to youtubeId),
            otherSong to otherServiceId
        )
    }

    @Test
    fun `original dictionary returned if a single song with both services`() {
        val initialDictionary = SongDictionary(
            song to ServiceIds(SPOTIFY to spotifyId, YOUTUBE_MUSIC to youtubeId)
        )
        val youTubeMusic = FakeYouTubeMusic(song to Id("2"), failOnSearch = true)

        val resultingDictionary = initialDictionary.fillDictionary(SPOTIFY, youTubeMusic)

        resultingDictionary shouldBe initialDictionary.withNoErrors()
    }

    @Test
    fun `original dictionary returned if multiple songs with both services`() {
        val initialDictionary = SongDictionary(
            song to ServiceIds(SPOTIFY to spotifyId, YOUTUBE_MUSIC to youtubeId),
            otherSong to ServiceIds(SPOTIFY to spotifyId, YOUTUBE_MUSIC to youtubeId),
        )
        val youTubeMusic = FakeYouTubeMusic(song to youtubeId, failOnSearch = true)

        val resultingDictionary = initialDictionary.fillDictionary(SPOTIFY, youTubeMusic)

        resultingDictionary shouldBe initialDictionary.withNoErrors()
    }

    @Test
    fun `searches for a song if the service id is missing for the target`() {
        val initialDictionary = SongDictionary(
            song to ServiceIds(SPOTIFY to spotifyId),
        )
        val expectedDictionary = SongDictionary(
            song to ServiceIds(SPOTIFY to spotifyId, YOUTUBE_MUSIC to youtubeId),
        )
        val youTubeMusic = FakeYouTubeMusic(song to youtubeId)

        val resultingDictionary = initialDictionary.fillDictionary(SPOTIFY, youTubeMusic)

        resultingDictionary shouldBe expectedDictionary.withNoErrors()
    }

    @Test
    fun `returns dictionary with an unchanged song and a new song`() {
        val initialDictionary = SongDictionary(
            song to ServiceIds(SPOTIFY to spotifyId, YOUTUBE_MUSIC to youtubeId),
            otherSong to ServiceIds(SPOTIFY to spotifyId),
        )
        val expectedDictionary = SongDictionary(
            song to ServiceIds(SPOTIFY to spotifyId, YOUTUBE_MUSIC to youtubeId),
            otherSong to ServiceIds(SPOTIFY to spotifyId, YOUTUBE_MUSIC to otherId),
        )
        val youTubeMusic = FakeYouTubeMusic(otherSong to otherId)

        val resultingDictionary = initialDictionary.fillDictionary(SPOTIFY, youTubeMusic)

        resultingDictionary shouldBe expectedDictionary.withNoErrors()
    }

    @Test
    fun `ignores songs if no service id for the source system`() {
        val initialDictionary = SongDictionary(
            song to ServiceIds(YOUTUBE_MUSIC to youtubeId)
        )
        val youTubeMusic = FakeYouTubeMusic(song to otherId, failOnSearch = true)

        val resultingDictionary = initialDictionary.fillDictionary(SPOTIFY, youTubeMusic)

        resultingDictionary shouldBe initialDictionary.withNoErrors()
    }

    @Test
    fun `returns any errors from searching, along with the dictionary`() {
        val initialDictionary = SongDictionary(
            song to ServiceIds(SPOTIFY to spotifyId, YOUTUBE_MUSIC to youtubeId),
            otherSong to ServiceIds(SPOTIFY to spotifyId),
        )
        val youTubeMusic = FakeYouTubeMusic() // won't be able to find "otherSong"

        val resultingDictionary = initialDictionary.fillDictionary(SPOTIFY, youTubeMusic)

        resultingDictionary shouldBe ErrorWrapper(listOf(NoResultsError(otherSong)), initialDictionary)
    }

    @Test
    fun `taking a subset of a dictionary takes the song from the target`() {
        val source = SongDictionary(song to spotifyServiceId)
        val target = SongDictionary(song to youtubeServiceId)

        source.subsetOf(target) shouldBe target
    }

    @Test
    fun `if a song from the source is not in the target, then it is not returned`() {
        val source = SongDictionary(song to spotifyServiceId)
        val target = SongDictionary(otherSong to spotifyServiceId)

        source.subsetOf(target) shouldBe SongDictionary.empty()
    }

    @Test
    fun `takes the expected subset if the target dictionary is larger`() {
        val source = SongDictionary(song to spotifyServiceId)
        val target = SongDictionary(
            song to ServiceIds(SPOTIFY to spotifyId, YOUTUBE_MUSIC to youtubeId),
            otherSong to spotifyServiceId
        )

        source.subsetOf(target) shouldBe SongDictionary(
            song to ServiceIds(
                SPOTIFY to spotifyId,
                YOUTUBE_MUSIC to youtubeId
            ),
        )
    }
}


private class FakeYouTubeMusic private constructor(
    private val songs: Map<Song, Id>,
    private val failOnSearch: Boolean
) : MusicService {
    constructor(vararg songs: Pair<Song, Id>, failOnSearch: Boolean = false) : this(mapOf(*songs), failOnSearch)

    override val service: Service = YOUTUBE_MUSIC

    override fun playlists(): Either<Error, List<Playlist>> {
        TODO("Not needed")
    }

    override fun search(song: Song): Either<Error, SongDictionary> {
        if (failOnSearch) fail("Search was called but should not have been called: ${song.name.value}")

        val matchingId = songs[song]
        return if (matchingId != null) Either.Right(SongDictionary(song to ServiceIds(YOUTUBE_MUSIC to matchingId)))
        else Either.Left(NoResultsError(song))
    }
}