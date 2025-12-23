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
    playlists: List<Playlist>,
    allSongs: SongDictionary
) : MusicService {
    private val _playlists: MutableMap<Id, Playlist> = playlists.associate { playlist ->
        val filteredEntries = playlist.songs.entries.mapNotNull { (song, serviceIds) ->
            serviceIds.entries[service]?.let { id -> song to ServiceIds(service to id) }
        }.associate { it }

        val filteredPlaylist = playlist.copy(songs = SongDictionary(filteredEntries))
        playlist.id to filteredPlaylist
    }.toMutableMap()

    private val _songs: Map<Song, Id> = allSongs.entries.mapNotNull { (song, serviceIds) ->
        serviceIds.entries[service]?.let { id -> song to id }
    }.toMap()

    private val _songsById: Map<Id, Song> = _songs.entries.associate { (song, id) -> id to song }

    override fun playlists(): Either<Error, List<Playlist>> = Either.Right(_playlists.values.toList())

    override fun playlists(metadata: List<PlaylistMetadata>): Either<Error, List<Playlist>> =
        Either.Right(metadata.map { playlistMetadata ->
            _playlists[playlistMetadata.id] ?: Playlist(
                playlistMetadata.id,
                playlistMetadata.name,
                SongDictionary.empty()
            )
        })

    override fun playlistMetadata(): Either<Error, List<PlaylistMetadata>> =
        Either.Right(_playlists.values.map { PlaylistMetadata(it.id, it.name) })

    override fun search(song: Song): Either<Error, SongDictionary> {
        val id = _songs[song] ?: return NoResultsError(song).left()
        return SongDictionary(song to ServiceIds(service to id)).right()
    }

    override fun addSongToPlaylist(
        songId: Id,
        playlistId: Id
    ): Either<Error, Unit> {
        val playlist = _playlists[playlistId] ?: return NotFoundError.left()
        val song = _songsById[songId] ?: return NotFoundError.left()

        val newEntry = song to ServiceIds(service to songId)
        val newSongs = playlist.songs.entries + newEntry

        _playlists[playlistId] = playlist.copy(songs = SongDictionary(newSongs))
        return Unit.right()
    }

    override fun deleteSongFromPlaylist(songId: Id, playlistId: Id): Either<Error, Unit> {
        val playlist = _playlists[playlistId] ?: return NotFoundError.left()
        val song = _songsById[songId] ?: return NotFoundError.left()

        val newSongs = playlist.songs.entries - song
        _playlists[playlistId] = playlist.copy(songs = SongDictionary(newSongs))
        return Unit.right()
    }

    override fun tracks(playlistId: Id): Either<HttpError, SongDictionary> {
        val playlist = _playlists[playlistId] ?: return HttpResponseError(404, "Playlist not found").left()
        return playlist.songs.right()
    }
}
