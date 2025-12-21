package org.example.util

import org.example.log.Log
import kotlin.time.measureTimedValue

fun <T> time(name: String, log: Log, block: () -> T): T {
    log.info("starting $name")
    val timedValue = measureTimedValue(block)
    val seconds = timedValue.duration.inWholeMilliseconds / 1000.0
    log.info("finished $name in ${seconds}s")
    return timedValue.value
}
