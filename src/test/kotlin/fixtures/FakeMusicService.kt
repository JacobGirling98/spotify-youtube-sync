package fixtures

import arrow.core.Either
import org.example.domain.error.NoResultsError
import org.example.domain.model.*
import org.example.domain.music.MusicService

class FakeMusicService(
    override val service: Service,
    songs: Map<Song, Id>,
    private val playlists: Map<Name, Map<Song, Id>>
) : MusicService {
    private val dictionaries = songs.map { (song, id) -> SongDictionary(song to ServiceIds(service to id)) }

    override fun playlists(): Either<Error, List<Playlist>> = Either.Right(playlists)

    override fun search(song: Song): Either<Error, SongDictionary> = dictionaries
        .firstOrNull { it.entries.keys.first() == song }
        ?.let { Either.Right(it) } ?: Either.Left(NoResultsError(song))
}