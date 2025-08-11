package org.example.domain.music

import arrow.core.Either
import org.example.domain.error.Error
import org.example.domain.model.Playlist
import org.example.domain.model.Song
import org.example.domain.model.SongDictionary

interface MusicService {
    fun playlists(): Either<Error, List<Playlist>>
    fun search(song: Song): Either<Error, SongDictionary>
}