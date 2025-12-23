package org.example.domain.music

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.raise.either
import org.example.domain.error.Error
import org.example.domain.error.NotFoundError
import org.example.domain.error.PlaylistNotFoundError
import org.example.domain.error.SongNotFoundError
import org.example.domain.model.Name
import org.example.domain.model.SongDictionary
import org.example.log.Log
import org.example.repository.Repository

fun syncMusic(
    playlistsToSync: List<Name>,
    sourceService: MusicService,
    targetService: MusicService,
    songDictionaryRepository: Repository<SongDictionary>,
    log: Log
): Either<Error, Unit> = either {
    val sourcePlaylistMetadata = sourceService.playlistMetadata().bind()
    val targetPlaylistsMetadata = targetService.playlistMetadata().bind()

    log.info("Found ${sourcePlaylistMetadata.size} playlists in source service ${sourceService.service}")
    log.info("Found ${targetPlaylistsMetadata.size} playlists in target service ${targetService.service}")

    val dictionaryFromDiskOrEmpty = songDictionaryRepository.load().getOrElse { SongDictionary.empty() }

    val sourcePlaylists = sourceService.playlists(sourcePlaylistMetadata.filter { it.name in playlistsToSync }).bind()
    val targetPlaylists = targetService.playlists(targetPlaylistsMetadata.filter { it.name in playlistsToSync }).bind()

    log.info("Syncing ${sourcePlaylists.size} playlists from ${sourceService.service} to ${targetService.service}")

    // it.entries.filter { (_, ids) -> ids.services.size == 1 }
    // it.entries.filter { (song, ids) -> song.name == Name("Impermanence") } (multiple artists)


    val updatedDictionaryWithErrors = (sourcePlaylists + targetPlaylists).createDictionary()
        .flatMap { dictionaryFromDiskOrEmpty.mergeWith(it) }
        .map { it.fillDictionary(sourceService.service, targetService) }
        .bind()

    log.info("Finished building the song dictionary")

    updatedDictionaryWithErrors.errors.forEach { log.error(it.message ?: "An unknown error occurred") }

    val dictionary = updatedDictionaryWithErrors.value

    sourcePlaylists.forEach { sourcePlaylist ->
        val targetPlaylist = targetPlaylists.find { it.name == sourcePlaylist.name } ?: raise(
            PlaylistNotFoundError(
                sourcePlaylist.name,
                targetService.service
            )
        )
        val delta = sourcePlaylist.deltaWith(targetPlaylist)
        delta.removed.forEach { song ->
            val targetServiceSongId =
                dictionary.ids(song)?.idFor(targetService.service) ?: raise(SongNotFoundError(song, targetService.service))
            targetService.addSongToPlaylist(targetServiceSongId, targetPlaylist.id)
        }
        delta.added.forEach { song ->
            val targetServiceSongId =
                dictionary.ids(song)?.idFor(targetService.service) ?: raise(SongNotFoundError(song, targetService.service))
            targetService.deleteSongFromPlaylist(targetServiceSongId, targetPlaylist.id)
        }
    }

    songDictionaryRepository.save(dictionary)
}