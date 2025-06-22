package org.example.domain.music

import arrow.core.Either
import org.example.domain.model.Playlist
import org.example.http.auth.Error

interface MusicService {
    fun playlists(): Either<Error, List<Playlist>>
}