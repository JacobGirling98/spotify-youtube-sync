package org.example.config

import arrow.core.Either
import arrow.core.raise.either

sealed class ConfigVariable(val name: String)

data object Playlists : ConfigVariable("PLAYLISTS")

data class Properties(
    val playlists: List<String>
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
        val playlists = props.getProperty(Playlists.name)?.split(",")
            ?: raise(ConfigError.PlaylistsNotSet)
        Properties(playlists)
    }