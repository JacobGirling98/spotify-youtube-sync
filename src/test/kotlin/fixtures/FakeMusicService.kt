package fixtures

import arrow.core.Either
import org.example.domain.error.Error
import org.example.domain.error.NoResultsError
import org.example.domain.model.*
import org.example.domain.music.MusicService

class FakeMusicService(
    override val service: Service,
    songs: Map<Song, Id>,
    private val playlists: Map<Name, Map<Song, Id>>
) : MusicService {
    private val dictionaries = songs.map { (song, id) -> SongDictionary(song to ServiceIds(service to id)) }

    override fun playlists(): Either<Error, List<Playlist>> = Either.Right(playlists.map { (name, songs) ->
        Playlist(
            Id(name.value),
            name,
            SongDictionary(songs.map { (song, id) ->
                song to ServiceIds(service to id)
            }.associate { it })
        )
    })

    override fun playlistMetadata(): Either<Error, List<PlaylistMetadata>> = Either.Right(playlists.map { (name, _) ->
        PlaylistMetadata(Id(name.value), name)
    })

    override fun search(song: Song): Either<Error, SongDictionary> = dictionaries
        .firstOrNull { it.entries.keys.first() == song }
        ?.let { Either.Right(it) } ?: Either.Left(NoResultsError(song))

    override fun deletePlaylist(id: Id): Either<Error, Unit> {
        TODO("Not yet implemented")
    }

    override fun addSongToPlaylist(
        songId: Id,
        playlistId: Id
    ): Either<Error, Unit> {
        TODO("Not yet implemented")
    }
}