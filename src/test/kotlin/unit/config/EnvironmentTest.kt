package unit.config

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import org.example.config.EnvironmentVariables
import org.example.config.YouTubeClientIdNotSet
import org.example.config.YouTubeClientId
import org.example.config.loadEnvironmentVariables

class EnvironmentTest : DescribeSpec({
    afterEach { System.setProperty(YouTubeClientId.name, "") }

    describe("loading YOUTUBE_CLIENT_ID") {
        it("loads when variable is set") {
            loadEnvironmentVariables { envVariableMap[it] } shouldBeRight EnvironmentVariables("YOUTUBE-ID")
        }

        it("fails if variable is not set") {
            val envVariableNotSet = HashMap(envVariableMap).toMutableMap().apply { this[YouTubeClientId.name] = "" }

            loadEnvironmentVariables { envVariableNotSet[it] } shouldBeLeft YouTubeClientIdNotSet
        }
    }
})

private val envVariableMap = mutableMapOf(
    YouTubeClientId.name to "YOUTUBE-ID"
)