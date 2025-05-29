package util

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TestClock : Clock {
    var epochSeconds = 0L

    fun advanceTime(time: Duration) {
        this.epochSeconds += time.inWholeSeconds
    }

    override fun now(): Instant {
        return Instant.fromEpochSeconds(epochSeconds)
    }

}