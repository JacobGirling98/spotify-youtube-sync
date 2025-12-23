package fixtures

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.example.domain.error.Error
import org.example.domain.error.HttpError
import org.example.domain.error.HttpResponseError
import org.example.domain.error.NoResultsError
import org.example.domain.error.NotFoundError
import org.example.domain.model.*
import org.example.domain.music.MusicService

class FakeMusicService(
    override val service: Service,
    playlists: Map<Name, Map<Song, Id>>
) : MusicService {
    private val _playlists = playlists.mapValues { it.value.toMutableMap() }.toMutableMap()
    private val _songs = _playlists.values.flatMap { it.entries }.associate { it.key to it.value }.toMutableMap()
    private val _songsById = _songs.entries.associate { it.value to it.key }.toMutableMap()

    override fun playlists(): Either<Error, List<Playlist>> = Either.Right(_playlists.map { (name, songs) ->
        Playlist(
            Id(name.value),
            name,
            SongDictionary(songs.map { (song, id) ->
                song to ServiceIds(service to id)
            }.associate { it })
        )
    })

    override fun playlists(metadata: List<PlaylistMetadata>): Either<Error, List<Playlist>> =
        Either.Right(metadata.map { playlistMetadata ->
            Playlist(playlistMetadata.id, playlistMetadata.name, SongDictionary.empty())
        })

    override fun playlistMetadata(): Either<Error, List<PlaylistMetadata>> = Either.Right(_playlists.map { (name, _) ->
        PlaylistMetadata(Id(name.value), name)
    })

    override fun search(song: Song): Either<Error, SongDictionary> {
        val id = _songs[song] ?: return NoResultsError(song).left()
        return SongDictionary(song to ServiceIds(service to id)).right()
    }

    override fun addSongToPlaylist(
        songId: Id,
        playlistId: Id
    ): Either<Error, Unit> {
        val playlistName = Name(playlistId.value)
        val playlist = _playlists[playlistName] ?: return NotFoundError.left()
        val song = _songsById[songId] ?: return NotFoundError.left()

        playlist[song] = songId
        return Unit.right()
    }

    override fun tracks(playlistId: Id): Either<HttpError, SongDictionary> {
        val playlistName = Name(playlistId.value)
        val playlist = _playlists[playlistName] ?: return HttpResponseError(404, "Playlist not found").left()

        return SongDictionary(playlist.map { (song, id) ->
             song to ServiceIds(service to id)
        }.associate { it }).right()
    }
}