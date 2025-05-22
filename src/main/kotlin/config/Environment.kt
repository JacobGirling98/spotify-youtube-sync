package org.example.config

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure

sealed class EnvVariable(val name: String)
data object YouTubeClientId : EnvVariable("YOUTUBE_CLIENT_ID")
data object SpotifyClientId : EnvVariable("SPOTIFY_CLIENT_ID")

data class EnvironmentVariables(
    val youtubeClientId: String,
    val spotifyClientId: String
)

fun loadEnvironmentVariables(loadVariable: (String) -> String? = { System.getenv(it) }): Either<ConfigError, EnvironmentVariables> =
    either {
        val youtubeClientId = loadVariable(YouTubeClientId.name)
        val spotifyClientId = loadVariable(SpotifyClientId.name)

        ensure(!youtubeClientId.isNullOrBlank()) { ConfigError.YouTubeClientIdNotSet }
        ensure(!spotifyClientId.isNullOrBlank()) { ConfigError.SpotifyClientIdNotSet }

        EnvironmentVariables(youtubeClientId, spotifyClientId)
    }