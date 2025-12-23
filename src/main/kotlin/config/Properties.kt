package org.example.config

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import org.example.domain.model.Name

sealed class ConfigVariable(val name: String)

data object Playlists : ConfigVariable("playlists")
data object RedirectUri : ConfigVariable("redirect_uri")
data object RedirectPort : ConfigVariable("redirect_port")
data object SpotifyBaseUrl : ConfigVariable("spotify_base_url")
data object YouTubeBaseUrl : ConfigVariable("youtube_base_url")

data class Properties(
    val playlists: List<Name>,
    val redirectServerUri: String,
    val redirectServerPort: Int,
    val spotifyBaseUrl: String,
    val youtubeBaseUrl: String
)

fun propsFromClasspath(file: String): Either<ConfigError, java.util.Properties> = either {
    val props = java.util.Properties()
    val resourceStream = object {}.javaClass.getResourceAsStream(file)
        ?: raise(ConfigError.ConfigFileNotFound)
    resourceStream.use { props.load(it) }
    props
}

fun loadProperties(readProps: (String) -> Either<ConfigError, java.util.Properties> = ::propsFromClasspath): Either<ConfigError, Properties> =
    either {
        val props = readProps("/config.properties").bind()

        val playlists = getRequiredProperty(props, Playlists, ConfigError.PlaylistsNotSet).split(",").map { Name(it.trim()) }
        val redirectUri = getRequiredProperty(props, RedirectUri, ConfigError.RedirectUriNotSet)
        val redirectPort = getRequiredProperty(props, RedirectPort, ConfigError.RedirectPortNotSet).toIntOrNull()
            ?: raise(ConfigError.RedirectPortNotSet)
        val spotifyBaseUrl = getRequiredProperty(props, SpotifyBaseUrl, ConfigError.SpotifyBaseUrlNotSet)
        val youtubeBaseUrl = getRequiredProperty(props, YouTubeBaseUrl, ConfigError.YouTubeBaseUrlNotSet)

        Properties(playlists, redirectUri, redirectPort, spotifyBaseUrl, youtubeBaseUrl)
    }

private fun Raise<ConfigError>.getRequiredProperty(
    props: java.util.Properties,
    variable: ConfigVariable,
    error: ConfigError
): String {
    val property = props.getProperty(variable.name)
    ensure(!property.isNullOrBlank()) { error }
    return property
}

