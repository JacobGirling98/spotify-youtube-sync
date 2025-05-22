package org.example.config

sealed class ConfigError(val message: String) {
    data object YouTubeClientIdNotSet : ConfigError("YouTube client id env variable not set")
    data object SpotifyClientIdNotSet : ConfigError("Spotify client id env variable not set")
}
