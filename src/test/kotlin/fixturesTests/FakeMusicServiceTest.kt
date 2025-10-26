package fixturesTests

import fixtures.FakeMusicService
import fixtures.data.song
import org.example.domain.model.Id
import org.example.domain.model.Name
import org.example.domain.model.Service
import kotlin.test.Test

class FakeMusicServiceTest {

    private val firstId = Id("1")
    private val secondId = Id("2")
    private val thirdId = Id("3")
    private val fourthId = Id("4")
    private val fifthId = Id("5")

    private val firstSong = song("first song")
    private val secondSong = song("second song")
    private val thirdSong = song("third song")
    private val fourthSong = song("fourth song")
    private val fifthSong = song("fifth song")

    private val firstPlaylist = mapOf(Name("first playlist") to mapOf(firstSong to firstId))
    private val secondPlaylist = mapOf(Name("second playlist") to mapOf(secondSong to secondId))

    @Test
    fun `just returns the given playlists`() {
        val service = FakeMusicService(Service.SPOTIFY, mapOf(firstSong to firstId, secondSong to secondId))


    }
}