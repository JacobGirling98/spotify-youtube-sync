package org.example.config

import org.example.domain.model.Playlist
import org.example.domain.model.SongDictionary
import org.example.domain.music.MusicService
import org.example.http.auth.*
import org.example.http.server.redirectHandler
import org.example.http.server.routes
import org.example.http.spotify.client.SpotifyRestClient
import org.example.http.util.retry
import org.example.http.youtube.client.YouTubeRestClient
import org.example.log.KotlinLoggingLogger
import org.example.log.Log
import org.example.repository.Repository
import org.example.repository.playlistRepository
import org.example.repository.songDictionaryRepository
import org.example.util.TimedProxy
import org.http4k.client.ApacheClient
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import java.io.File
import kotlin.time.ExperimentalTime


data class Dependencies(
    val spotify: MusicService,
    val youTube: MusicService,
    val songDictionaryRepository: Repository<SongDictionary>,
    val spotifyPlaylistRepository: Repository<List<Playlist>>,
    val youTubePlaylistRepository: Repository<List<Playlist>>,
    val server: Http4kServer,
    val log: Log,
    val spotifyRedirectUri: String,
    val youTubeRedirectUri: String
)

@OptIn(ExperimentalTime::class)
fun loadDependencies(environment: EnvironmentVariables, properties: Properties): Dependencies {
    val log = KotlinLoggingLogger("app")
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
    ).asServer(Undertow(8000))

    val spotifyClient =
        TimedProxy.create<MusicService>(
            SpotifyRestClient(
                retry(client),
                spotifyTokenManager,
                "https://api.spotify.com/v1"
            ), log
        )
    val youTubeRestClient =
        TimedProxy.create<MusicService>(
            YouTubeRestClient(
                retry(client),
                youTubeTokenManager,
                "https://www.googleapis.com/youtube/v3"
            ), log
        )

    val songDictionaryRepository = songDictionaryRepository()
    val spotifyPlaylistRepository = playlistRepository(File("data/spotify-playlists.json"))
    val youtubePlaylistRepository = playlistRepository(File("data/youtube-playlists.json"))

    return Dependencies(
        spotify = spotifyClient,
        youTube = youTubeRestClient,
        songDictionaryRepository = songDictionaryRepository,
        spotifyPlaylistRepository = spotifyPlaylistRepository,
        youTubePlaylistRepository = youtubePlaylistRepository,
        server = app,
        log = log,
        spotifyRedirectUri = spotifyConfig.codeUri(environment.spotifyClientId),
        youTubeRedirectUri = youtubeConfig.codeUri(environment.youtubeClientId)
    )
}