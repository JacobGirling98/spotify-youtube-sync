package org.example

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.raise.either
import org.example.config.loadDependencies
import org.example.config.loadEnvironmentVariables
import org.example.config.loadProperties
import org.example.domain.error.Error
import org.example.domain.model.ErrorWrapper
import org.example.domain.model.Playlist
import org.example.domain.model.Service
import org.example.domain.model.SongDictionary
import org.example.domain.music.MusicService
import org.example.domain.music.createDictionary
import org.example.domain.music.fillDictionary
import org.example.log.Log
import org.example.repository.Repository
import java.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main() {
    val environment = loadEnvironmentVariables().getOrElse { error(it.message) }
    val properties = loadProperties().getOrElse { error(it.message) }

    val (spotifyClient, youTubeRestClient, songDictionaryRepository, spotifyPlaylistRepository, youtubePlaylistRepository, server, log, spotifyRedirectUri, youTubeRedirectUri) = loadDependencies(
        environment,
        properties
    )

    server.start()

    while (true) {
        log.info(spotifyRedirectUri)
        log.info(youTubeRedirectUri)

        Thread.sleep(Duration.ofSeconds(20))
        sync(
            spotifyClient,
            youTubeRestClient,
            songDictionaryRepository,
            spotifyPlaylistRepository,
            youtubePlaylistRepository,
            log
        )
    }
}

private fun unifyDictionary(
    spotifyPlaylists: Either<Error, List<Playlist>>,
    youtubePlaylists: Either<Error, List<Playlist>>,
    youTubeRestClient: MusicService,
    log: Log
): Either<Error, ErrorWrapper<SongDictionary>> = either { spotifyPlaylists.bind() + youtubePlaylists.bind() }
    .flatMap { it.createDictionary() }
    .map { it.fillDictionary(Service.SPOTIFY, youTubeRestClient) }

private fun sync(
    spotifyClient: MusicService,
    youTubeRestClient: MusicService,
    songDictionaryRepository: Repository<SongDictionary>,
    spotifyPlaylistRepository: Repository<List<Playlist>>,
    youtubePlaylistRepository: Repository<List<Playlist>>,
    log: Log
) {
//    val spotifyPlaylists = spotifyClient.playlists()
//    val youtubePlaylists = youTubeRestClient.playlists()

    val spotifyPlaylists = spotifyPlaylistRepository.load()
    val youtubePlaylists = youtubePlaylistRepository.load()

    spotifyPlaylists.fold(
        { error -> log.error("$error") },
        { playlists -> spotifyPlaylistRepository.save(playlists) }
    )
    youtubePlaylists.fold(
        { error -> log.error("$error") },
        { playlists -> youtubePlaylistRepository.save(playlists) }
    )

    log.info("Saved to playlist repositories")

    val unifyResult = unifyDictionary(spotifyPlaylists, youtubePlaylists, youTubeRestClient, log)

    log.info("Dictionaries are combined")

    unifyResult.fold(
        { error -> log.error("Error unifying dictionary: ${error.message}") },
        { errorWrapper ->
            val songDictionary = errorWrapper.value
            songDictionaryRepository.save(songDictionary).fold(
                { error -> log.error("Error saving song dictionary: ${error.message}") },
                { log.info("Song dictionary saved successfully.") }
            )
        }
    )

    // loop over spotify playlists
    // if that playlist exists in youtube, delete
    // create new playlist on youtube
    // add songs to it
}

