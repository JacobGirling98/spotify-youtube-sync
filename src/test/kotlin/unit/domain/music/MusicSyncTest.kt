package unit.domain.music

import fixtures.FakeLog
import fixtures.FakeMusicService
import fixtures.InMemoryRepository
import fixtures.data.playlist
import fixtures.data.song
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.example.domain.model.*
import org.example.domain.model.Service.SPOTIFY
import org.example.domain.model.Service.YOUTUBE_MUSIC
import org.example.domain.music.syncMusic
import kotlin.test.Test

class MusicSyncTest {

    private val spotifyPlaylistIdA = Id("spotify-playlist-A")
    private val spotifyPlaylistIdB = Id("spotify-playlist-B")
    private val youTubePlaylistIdA = Id("youtube-playlist-A")
    private val youTubePlaylistIdB = Id("youtube-playlist-B")

    private val spotifySongIdA = Id("spotify-song-id-a")
    private val spotifySongIdB = Id("spotify-song-id-b")
    private val youTubeSongIdA = Id("youtube-song-id-a")
    private val youTubeSongIdB = Id("youtube-song-id-b")

    private val songA = song("Song A")
    private val songB = song("Song B")

    private val allSongs = SongDictionary(
        songA to ServiceIds(SPOTIFY to spotifySongIdA, YOUTUBE_MUSIC to youTubeSongIdA),
        songB to ServiceIds(SPOTIFY to spotifySongIdB, YOUTUBE_MUSIC to youTubeSongIdB)
    )

    private val playlistName = "My Playlist"
    private val otherPlaylistName = "Other Playlist"

    private fun songsFor(service: Service, songs: List<Pair<Song, Id>>) = SongDictionary(
        songs.associate { (song, id) -> song to ServiceIds(service to id) }
    )

    private val log = FakeLog()

    @Test
    fun `adds new song to the target playlist if the source is different`() {
        val spotifyPlaylist = playlist(
            spotifyPlaylistIdA,
            playlistName,
            songsFor(
                SPOTIFY, listOf(
                    songA to spotifySongIdA,
                    songB to spotifySongIdB
                )
            )
        )
        val youTubePlaylist = playlist(
            youTubePlaylistIdA,
            playlistName,
            songsFor(
                YOUTUBE_MUSIC, listOf(
                    songA to youTubeSongIdA,
                )
            )
        )

        val spotify = FakeMusicService(SPOTIFY, listOf(spotifyPlaylist), allSongs)
        val youTube = FakeMusicService(YOUTUBE_MUSIC, listOf(youTubePlaylist), allSongs)
        val dictionary = SongDictionary(
            songA to ServiceIds(SPOTIFY to spotifySongIdA, YOUTUBE_MUSIC to youTubeSongIdA),
            songB to ServiceIds(SPOTIFY to spotifySongIdB, YOUTUBE_MUSIC to youTubeSongIdB)
        )
        val dictionaryRepository = InMemoryRepository<SongDictionary>().apply { save(dictionary) }

        syncMusic(
            playlistsToSync = listOf(Name(playlistName)),
            sourceService = spotify,
            targetService = youTube,
            songDictionaryRepository = dictionaryRepository,
            log = log
        )

        youTube.playlists().shouldBeRight() shouldContainExactly listOf(
            playlist(
                youTubePlaylistIdA,
                playlistName,
                SongDictionary(
                    songA to ServiceIds(YOUTUBE_MUSIC to youTubeSongIdA),
                    songB to ServiceIds(YOUTUBE_MUSIC to youTubeSongIdB)
                )
            )
        )
    }

    @Test
    fun `removes songs from the taret playlist if the source is different`() {
        val spotifyPlaylist = playlist(
            spotifyPlaylistIdA,
            playlistName,
            SongDictionary(
                songA to ServiceIds(SPOTIFY to spotifySongIdA),
            )
        )
        val youTubePlaylist = playlist(
            youTubePlaylistIdA,
            playlistName,
            SongDictionary(
                songA to ServiceIds(YOUTUBE_MUSIC to youTubeSongIdA),
                songB to ServiceIds(YOUTUBE_MUSIC to youTubeSongIdB)
            )
        )

        val spotify = FakeMusicService(SPOTIFY, listOf(spotifyPlaylist), allSongs)
        val youTube = FakeMusicService(YOUTUBE_MUSIC, listOf(youTubePlaylist), allSongs)
        val dictionary = SongDictionary(
            songA to ServiceIds(SPOTIFY to spotifySongIdA, YOUTUBE_MUSIC to youTubeSongIdA),
            songB to ServiceIds(SPOTIFY to spotifySongIdB, YOUTUBE_MUSIC to youTubeSongIdB)
        )
        val dictionaryRepository = InMemoryRepository<SongDictionary>().apply { save(dictionary) }

        syncMusic(
            playlistsToSync = listOf(Name(playlistName)),
            sourceService = spotify,
            targetService = youTube,
            songDictionaryRepository = dictionaryRepository,
            log = log
        )

        youTube.playlists().shouldBeRight() shouldContainExactly listOf(
            playlist(
                youTubePlaylistIdA,
                playlistName,
                SongDictionary(
                    songA to ServiceIds(YOUTUBE_MUSIC to youTubeSongIdA),
                )
            )
        )
    }

    @Test
    fun `syncs multiple playlists with additions and deletions`() {
        val spotifyPlaylistA = playlist(
            spotifyPlaylistIdA,
            playlistName,
            songsFor(
                SPOTIFY, listOf(
                    songA to spotifySongIdA,
                )
            )
        )
        val spotifyPlaylistB = playlist(
            spotifyPlaylistIdB,
            otherPlaylistName,
            songsFor(
                SPOTIFY, listOf(
                    songB to spotifySongIdB,
                )
            )
        )
        val youTubePlaylistA = playlist(
            youTubePlaylistIdA,
            playlistName,
            songsFor(
                YOUTUBE_MUSIC, listOf(
                    songB to youTubeSongIdB,
                )
            )
        )
        val youTubePlaylistB = playlist(
            youTubePlaylistIdB,
            otherPlaylistName,
            songsFor(
                YOUTUBE_MUSIC, listOf(
                    songA to youTubeSongIdA,
                )
            )
        )

        val spotify = FakeMusicService(SPOTIFY, listOf(spotifyPlaylistA, spotifyPlaylistB), allSongs)
        val youTube = FakeMusicService(YOUTUBE_MUSIC, listOf(youTubePlaylistA, youTubePlaylistB), allSongs)
        val dictionary = SongDictionary(
            songA to ServiceIds(SPOTIFY to spotifySongIdA, YOUTUBE_MUSIC to youTubeSongIdA),
            songB to ServiceIds(SPOTIFY to spotifySongIdB, YOUTUBE_MUSIC to youTubeSongIdB)
        )
        val dictionaryRepository = InMemoryRepository<SongDictionary>().apply { save(dictionary) }

        syncMusic(
            playlistsToSync = listOf(Name(playlistName), Name(otherPlaylistName)),
            sourceService = spotify,
            targetService = youTube,
            songDictionaryRepository = dictionaryRepository,
            log = log
        )

        youTube.playlists().shouldBeRight() shouldContainExactly listOf(
            playlist(
                youTubePlaylistIdA,
                playlistName,
                SongDictionary(
                    songA to ServiceIds(YOUTUBE_MUSIC to youTubeSongIdA)
                )
            ), playlist(
                youTubePlaylistIdB,
                otherPlaylistName,
                SongDictionary(
                    songB to ServiceIds(YOUTUBE_MUSIC to youTubeSongIdB)
                )
            )
        )
    }

    @Test
    fun `searches for a song to add if it's not in the song dictionary and saves the new dictionary`() {
        val spotifyPlaylist = playlist(
            spotifyPlaylistIdA,
            playlistName,
            songsFor(
                SPOTIFY, listOf(
                    songA to spotifySongIdA,
                    songB to spotifySongIdB
                )
            )
        )
        val youTubePlaylist = playlist(
            youTubePlaylistIdA,
            playlistName,
            songsFor(
                YOUTUBE_MUSIC, listOf(
                    songA to youTubeSongIdA,
                )
            )
        )

        val spotify = FakeMusicService(SPOTIFY, listOf(spotifyPlaylist), allSongs)
        val youTube = FakeMusicService(YOUTUBE_MUSIC, listOf(youTubePlaylist), allSongs)
        val dictionaryMissingYouTubeSongB = SongDictionary(
            songA to ServiceIds(SPOTIFY to spotifySongIdA, YOUTUBE_MUSIC to youTubeSongIdA),
            songB to ServiceIds(SPOTIFY to spotifySongIdB)
        )
        val dictionaryRepository = InMemoryRepository<SongDictionary>().apply { save(dictionaryMissingYouTubeSongB) }

        syncMusic(
            playlistsToSync = listOf(Name(playlistName)),
            sourceService = spotify,
            targetService = youTube,
            songDictionaryRepository = dictionaryRepository,
            log = log
        ).shouldBeRight()

        youTube.playlists().shouldBeRight() shouldContainExactly listOf(
            playlist(
                youTubePlaylistIdA,
                playlistName,
                SongDictionary(
                    songA to ServiceIds(YOUTUBE_MUSIC to youTubeSongIdA),
                    songB to ServiceIds(YOUTUBE_MUSIC to youTubeSongIdB)
                )
            )
        )

        dictionaryRepository.load().shouldBeRight() shouldBe SongDictionary(
            songA to ServiceIds(SPOTIFY to spotifySongIdA, YOUTUBE_MUSIC to youTubeSongIdA),
            songB to ServiceIds(SPOTIFY to spotifySongIdB, YOUTUBE_MUSIC to youTubeSongIdB)
        )
    }
}