package org.example.config

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure

sealed class ConfigException(override val message: String) : RuntimeException(message)
data object YouTubeClientIdNotSet : ConfigException("YouTube client id env variable not set") {
    private fun readResolve(): Any = YouTubeClientIdNotSet
}

sealed class EnvVariable(val name: String)
data object YouTubeClientId : EnvVariable("YOUTUBE_CLIENT_ID")

data class EnvironmentVariables(
    val youtubeClientId: String
)

fun loadEnvironmentVariables(loadVariable: (String) -> String?): Either<ConfigException, EnvironmentVariables> =
    either {
        val youtubeClientId = loadVariable(YouTubeClientId.name)

        ensure(!youtubeClientId.isNullOrBlank()) { YouTubeClientIdNotSet }

        EnvironmentVariables(youtubeClientId)
    }