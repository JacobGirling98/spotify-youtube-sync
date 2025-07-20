package unit.domain.music

import fixtures.*
import io.kotest.assertions.arrow.core.shouldBeRight
import org.example.domain.model.Id
import org.example.domain.model.Service.SPOTIFY
import org.example.domain.model.Service.YOUTUBE_MUSIC
import org.example.domain.model.ServiceIds
import org.example.domain.model.SongDictionary
import org.example.domain.music.createDictionary
import kotlin.test.Test

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
}