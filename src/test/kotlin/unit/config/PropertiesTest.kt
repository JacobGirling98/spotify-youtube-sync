package unit.config

import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.getOrElse
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import org.example.config.ConfigError
import org.example.config.loadProperties
import org.example.config.propsFromClasspath
import java.util.*
import kotlin.test.Test
import kotlin.test.fail

class PropertiesTest {

    val props = Properties()

    @Test
    fun `returns an error when properties file is not found`() {
        val readFailure = { Left(ConfigError.ConfigFileNotFound) }

        loadProperties(readFailure) shouldBeLeft ConfigError.ConfigFileNotFound
    }

    @Test
    fun `returns properties with a single playlist`() {
        val read = {
            props.apply {
                setProperty("PLAYLISTS", "MyPlaylist")
            }
            Right(props)
        }

        loadProperties(read) shouldBeRight org.example.config.Properties(listOf("MyPlaylist"))
    }

    @Test
    fun `returns properties with multiple playlists`() {
        val read = {
            props.apply {
                setProperty("PLAYLISTS", "MyPlaylist,AnotherPlaylist,ThirdPlaylist")
            }
            Right(props)
        }

        loadProperties(read) shouldBeRight org.example.config.Properties(
            listOf(
                "MyPlaylist",
                "AnotherPlaylist",
                "ThirdPlaylist"
            )
        )
    }

    @Test
    fun `returns error if playlists is not set`() {
        val read = { Right(props) }

        loadProperties(read) shouldBeLeft ConfigError.PlaylistsNotSet
    }

    @Test
    fun `propsFromClasspath loads properties from existing file`() {
        val result = propsFromClasspath("/test.properties").getOrElse { fail("Should have loaded properties") }

        result.getProperty("key") shouldBe "value"
        result.getProperty("foo") shouldBe "bar"
    }

    @Test
    fun `propsFromClasspath returns ConfigFileNotFound for missing file`() {
        val result = propsFromClasspath("/non_existent.properties")

        result shouldBeLeft ConfigError.ConfigFileNotFound
    }
}