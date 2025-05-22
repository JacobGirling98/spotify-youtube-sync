package unit.config

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import org.example.config.*
import kotlin.test.Test

class EnvironmentTest {
    @Test
    fun `loads when all variables are set`() {
        loadEnvironmentVariables { envVariableMap[it] } shouldBeRight EnvironmentVariables("YOUTUBE-ID", "SPOTIFY-ID")
    }

    @Test
    fun `fails to load when YOUTUBE_CLIENT_ID is not set`() {
        val envVariableNotSet = envVariableMap.withEmptyValueOf(YouTubeClientId)

        loadEnvironmentVariables { envVariableNotSet[it] } shouldBeLeft ConfigError.YouTubeClientIdNotSet
    }

    @Test
    fun `fails to load when SPOTIFY_CLIENT_ID is not set`() {
        val envVariableNotSet = envVariableMap.withEmptyValueOf(SpotifyClientId)

        loadEnvironmentVariables { envVariableNotSet[it] } shouldBeLeft ConfigError.SpotifyClientIdNotSet
    }

}

private val envVariableMap = mutableMapOf(
    YouTubeClientId.name to "YOUTUBE-ID",
    SpotifyClientId.name to "SPOTIFY-ID"
)

private fun Map<String, String>.withEmptyValueOf(envVariable: EnvVariable) =
    HashMap(this).toMutableMap().apply { this[envVariable.name] = "" }