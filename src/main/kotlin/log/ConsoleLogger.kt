package org.example.log

import io.github.oshai.kotlinlogging.KotlinLogging

class ConsoleLogger : Logger {
    private val logger = KotlinLogging.logger { }

    override fun info(message: String) {
        logger.info { message }
    }
}