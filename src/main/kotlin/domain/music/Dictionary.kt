package org.example.domain.music

import arrow.core.Either
import arrow.core.raise.either
import org.example.domain.error.MergeError
import org.example.domain.model.Playlist
import org.example.domain.model.SongDictionary

fun createDictionary(playlists: List<Playlist>): Either<MergeError, SongDictionary> = either {
    playlists.fold(SongDictionary.empty()) { acc, playlist -> acc.mergeWith(playlist.songs).bind() }
}