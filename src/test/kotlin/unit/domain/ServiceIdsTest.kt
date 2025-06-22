package unit.domain

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import org.example.domain.error.MergeError
import org.example.domain.model.Id
import org.example.domain.model.Service.SPOTIFY
import org.example.domain.model.Service.YOUTUBE_MUSIC
import org.example.domain.model.ServiceIds
import kotlin.test.Test

class ServiceIdsTest {

    @Test
    fun `can combine two services`() {
        val first = ServiceIds(SPOTIFY to Id("123"))
        val second = ServiceIds(YOUTUBE_MUSIC to Id("456"))

        first.mergeWith(second) shouldBeRight ServiceIds(
            SPOTIFY to Id("123"),
            YOUTUBE_MUSIC to Id("456")
        )
    }

    @Test
    fun `same service isn't duplicated`() {
        val first = ServiceIds(SPOTIFY to Id("123"))
        val second = ServiceIds(SPOTIFY to Id("123"))

        first.mergeWith(second) shouldBeRight ServiceIds(
            SPOTIFY to Id("123")
        )
    }

    @Test
    fun `fails if ids are different for same service`() {
        val first = ServiceIds(SPOTIFY to Id("123"))
        val second = ServiceIds(SPOTIFY to Id("456"))

        first.mergeWith(second) shouldBeLeft MergeError("Ids do not match for same service when attempted to merge ServiceIds: 123, 456")
    }
}