package unit.domain.music

import fixtures.FakeMusicService
import fixtures.data.playlist
import fixtures.data.serviceIds
import fixtures.data.song
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.collections.shouldContainExactly
import org.example.domain.model.Id
import org.example.domain.model.Name
import org.example.domain.model.Service.SPOTIFY
import org.example.domain.model.Service.YOUTUBE_MUSIC
import org.example.domain.model.ServiceIds
import org.example.domain.model.SongDictionary
import org.example.domain.music.syncMusic
import kotlin.test.Test

class MusicSyncTest {

    val spotifyPlaylistId = Id("spotify-playlist-1")
    val youTubePlaylistId = Id("youtube-playlist-1")

    val spotifySongIdA = Id("spotify-song-id-a")
    val spotifySongIdB = Id("spotify-song-id-b")
    val youTubeSongIdA = Id("youtube-song-id-a")
    val youTubeSongIdB = Id("youtube-song-id-b")

    val songA = song("Song A")
    val songB = song("Song B")

    @Test
    fun `adds new song to the target playlist if the source is different`() {
        val name = "My Playlist"
        val spotifyPlaylist = playlist(
            spotifyPlaylistId,
            name,
            SongDictionary(songA to serviceIds(SPOTIFY), songB to serviceIds(SPOTIFY))
        )
        val youTubePlaylist = playlist(
            youTubePlaylistId,
            name,
            SongDictionary(songA to serviceIds(YOUTUBE_MUSIC))
        )

        val spotify = FakeMusicService(SPOTIFY, listOf(spotifyPlaylist))
        val youTube = FakeMusicService(YOUTUBE_MUSIC, listOf(youTubePlaylist))

        val dictionary = SongDictionary(
            songA to ServiceIds(SPOTIFY to spotifySongIdA, YOUTUBE_MUSIC to youTubeSongIdA),
            songB to ServiceIds(SPOTIFY to spotifySongIdB, YOUTUBE_MUSIC to youTubeSongIdB)
        )

        syncMusic(
            playlistsToSync = listOf(Name(name)),
            sourceService = spotify,
            targetService = youTube,
            dictionary = dictionary
        )

        youTube.playlists().shouldBeRight() shouldContainExactly listOf(
            playlist(
                youTubePlaylistId,
                name,
                SongDictionary(
                    songA to ServiceIds(YOUTUBE_MUSIC to youTubeSongIdA),
                    songB to ServiceIds(YOUTUBE_MUSIC to youTubeSongIdB)
                )
            )
        )
    }


}