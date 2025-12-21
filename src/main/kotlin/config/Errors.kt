package org.example.config

sealed class ConfigError(val message: String) {
    data object YouTubeClientIdNotSet : ConfigError("YouTube client id env variable not set")
    data object SpotifyClientIdNotSet : ConfigError("Spotify client id env variable not set")
    data object YouTubeClientSecretNotSet : ConfigError("YouTube client secret env variable not set")
    data object SpotifyClientSecretNotSet : ConfigError("Spotify client secret env variable not set")

    data object ConfigFileNotFound : ConfigError("Config file not found")
    data object PlaylistsNotSet : ConfigError("Playlists config variable not set")
}

