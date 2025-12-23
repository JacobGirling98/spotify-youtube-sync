package org.example.domain.music

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import org.example.domain.error.Error
import org.example.domain.error.NotFoundError
import org.example.domain.model.Name
import org.example.domain.model.SongDictionary
import org.example.repository.Repository

fun syncMusic(
    playlistsToSync: List<Name>,
    sourceService: MusicService,
    targetService: MusicService,
    songDictionaryRepository: Repository<SongDictionary>
): Either<Error, Unit> = either {
    val sourcePlaylistMetadata = sourceService.playlistMetadata().bind()
    val targetPlaylistsMetadata = targetService.playlistMetadata().bind()

    val dictionary = songDictionaryRepository.load().getOrElse { SongDictionary.empty() }

    val sourcePlaylists = sourceService.playlists(sourcePlaylistMetadata.filter { it.name in playlistsToSync }).bind()
    val targetPlaylists = targetService.playlists(targetPlaylistsMetadata.filter { it.name in playlistsToSync }).bind()

    sourcePlaylists.forEach { sourcePlaylist ->
        val targetPlaylist = targetPlaylists.find { it.name == sourcePlaylist.name } ?: raise(NotFoundError)
        val delta = sourcePlaylist.deltaWith(targetPlaylist)
        delta.removed.forEach { song ->
            val targetServiceSongId = dictionary.ids(song)?.idFor(targetService.service) ?: raise(NotFoundError)
            targetService.addSongToPlaylist(targetServiceSongId, targetPlaylist.id)
        }
        delta.added.forEach { song ->
            val targetServiceSongId = dictionary.ids(song)?.idFor(targetService.service) ?: raise(NotFoundError)
            targetService.deleteSongFromPlaylist(targetServiceSongId, targetPlaylist.id)
        }
    }
}