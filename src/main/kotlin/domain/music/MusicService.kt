package org.example.domain.music

import arrow.core.Either
import org.example.domain.error.Error
import org.example.domain.model.Id
import org.example.domain.model.Playlist
import org.example.domain.model.Service
import org.example.domain.model.Song
import org.example.domain.model.SongDictionary

interface MusicService {
    val service: Service

    fun playlists(): Either<Error, List<Playlist>>
    fun search(song: Song): Either<Error, SongDictionary>
    fun deletePlaylist(id: Id): Either<Error, Unit>
}