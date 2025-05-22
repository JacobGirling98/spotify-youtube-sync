package org.example.config

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure

sealed class EnvVariable(val name: String)

data object YouTubeClientId : EnvVariable("YOUTUBE_CLIENT_ID")
data object YouTubeClientSecret : EnvVariable("YOUTUBE_CLIENT_SECRET")

data object SpotifyClientId : EnvVariable("SPOTIFY_CLIENT_ID")
data object SpotifyClientSecret : EnvVariable("SPOTIFY_CLIENT_SECRET")

data class EnvironmentVariables(
    val youtubeClientId: String,
    val spotifyClientId: String,
    val youtubeClientSecret: String,
    val spotifyClientSecret: String
)

fun loadEnvironmentVariables(loadVariable: (String) -> String? = { System.getenv(it) }): Either<ConfigError, EnvironmentVariables> =
    either {
        val youtubeClientId = loadVariable(YouTubeClientId.name)
        val spotifyClientId = loadVariable(SpotifyClientId.name)
        val youtubeClientSecret = loadVariable(YouTubeClientSecret.name)
        val spotifyClientSecret = loadVariable(SpotifyClientSecret.name)

        ensure(!youtubeClientId.isNullOrBlank()) { ConfigError.YouTubeClientIdNotSet }
        ensure(!spotifyClientId.isNullOrBlank()) { ConfigError.SpotifyClientIdNotSet }
        ensure(!youtubeClientSecret.isNullOrBlank()) { ConfigError.YouTubeClientSecretNotSet }
        ensure(!spotifyClientSecret.isNullOrBlank()) { ConfigError.SpotifyClientSecretNotSet }

        EnvironmentVariables(youtubeClientId, spotifyClientId, youtubeClientSecret, spotifyClientSecret)
    }