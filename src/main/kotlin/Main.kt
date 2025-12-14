package org.example

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.raise.either
import org.example.config.loadEnvironmentVariables
import org.example.domain.error.Error
import org.example.domain.error.HttpError
import org.example.domain.model.ErrorWrapper
import org.example.domain.model.Playlist
import org.example.domain.model.Service
import org.example.domain.model.SongDictionary
import org.example.domain.music.createDictionary
import org.example.domain.music.fillDictionary
import org.example.http.auth.*
import org.example.http.server.redirectHandler
import org.example.http.server.routes
import org.example.http.spotify.client.SpotifyRestClient
import org.example.http.util.retry
import org.example.http.youtube.client.YouTubeRestClient
import org.example.repository.Repository
import org.example.repository.playlistRepository
import org.example.repository.songDictionaryRepository
import org.http4k.client.ApacheClient
import org.http4k.server.Undertow
import org.http4k.server.asServer
import java.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main() {
    val environment = loadEnvironmentVariables().getOrElse { error(it.message) }
    val serverUri = "http://127.0.0.1:8000"

    val spotifyConfig = SpotifyAuth(serverUri)
    val youtubeConfig = YouTubeAuth(serverUri)

    val client = ApacheClient()

    fun spotifyFetchToken(authCode: AuthCode) = getToken(
        authCode,
        spotifyConfig.tokenUri,
        spotifyConfig.redirectUri,
        environment.spotifyClientId,
        environment.spotifyClientSecret,
        client
    )

    fun spotifyRefreshToken(refreshToken: RefreshToken) = refreshToken(
        refreshToken,
        spotifyConfig.tokenUri,
        environment.spotifyClientId,
        environment.spotifyClientSecret,
        client
    )

    fun youtubeFetchToken(authCode: AuthCode) = getToken(
        authCode,
        youtubeConfig.tokenUri,
        youtubeConfig.redirectUri,
        environment.youtubeClientId,
        environment.youtubeClientSecret,
        client
    )

    fun youtubeRefreshToken(refreshToken: RefreshToken) = refreshToken(
        refreshToken,
        youtubeConfig.tokenUri,
        environment.youtubeClientId,
        environment.youtubeClientSecret,
        client
    )

    val spotifyTokenManager = OAuthTokenManager(::spotifyFetchToken, ::spotifyRefreshToken)
    val youTubeTokenManager = OAuthTokenManager(::youtubeFetchToken, ::youtubeRefreshToken)

    val app = routes(
        spotifyConfig,
        youtubeConfig,
        redirectHandler { spotifyTokenManager.updateAuthCode(it) },
        redirectHandler { youTubeTokenManager.updateAuthCode(it) }
    ).asServer(Undertow(8000)).start()

    val spotifyClient = SpotifyRestClient(retry(client), spotifyTokenManager, "https://api.spotify.com/v1")
    val youTubeRestClient =
        YouTubeRestClient(retry(client), youTubeTokenManager, "https://www.googleapis.com/youtube/v3")

    // Instantiate repositories
    val songDictionaryRepository = songDictionaryRepository()
    val playlistRepository = playlistRepository()

    println(youtubeConfig.codeUri(environment.youtubeClientId))

    while (true) {
        sync(spotifyClient, youTubeRestClient, songDictionaryRepository, playlistRepository) // Call sync function
    }
}

private fun unifyDictionary(
    spotifyPlaylists: Either<HttpError, List<Playlist>>,
    youtubePlaylists: Either<Error, List<Playlist>>,
    youTubeRestClient: YouTubeRestClient
): Either<Error, ErrorWrapper<SongDictionary>> = either { spotifyPlaylists.bind() + youtubePlaylists.bind() }
    .flatMap { it.createDictionary() }
    .map { it.fillDictionary(Service.SPOTIFY, youTubeRestClient) }

private fun sync(
    spotifyClient: SpotifyRestClient,
    youTubeRestClient: YouTubeRestClient,
    songDictionaryRepository: Repository<SongDictionary>,
    playlistRepository: Repository<List<Playlist>>
) {
    Thread.sleep(Duration.ofSeconds(20))

    val spotifyPlaylists = spotifyClient.playlists()
    val youtubePlaylists = youTubeRestClient.playlists()

    val unifyResult = unifyDictionary(spotifyPlaylists, youtubePlaylists, youTubeRestClient)

    unifyResult.fold(
        { error -> println("Error unifying dictionary: ${error.message}") },
        { errorWrapper ->
            val songDictionary = errorWrapper.value
            songDictionaryRepository.save(songDictionary).fold(
                { error -> println("Error saving song dictionary: ${error.message}") },
                { println("Song dictionary saved successfully.") }
            )

            val allPlaylists = spotifyPlaylists.getOrElse { emptyList() } + youtubePlaylists.getOrElse { emptyList() }
            playlistRepository.save(allPlaylists).fold(
                { error -> println("Error saving playlists: ${error.message}") },
                { println("Playlists saved successfully.") }
            )
        }
    )

    // loop over spotify playlists
    // if that playlist exists in youtube, delete
    // create new playlist on youtube
    // add songs to it
}

