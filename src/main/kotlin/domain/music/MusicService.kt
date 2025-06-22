package org.example.domain.music

import arrow.core.Either
import org.example.domain.error.Error
import org.example.domain.model.Playlist

interface MusicService {
    fun playlists(): Either<Error, List<Playlist>>
}