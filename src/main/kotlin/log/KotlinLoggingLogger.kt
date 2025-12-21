package org.example.log

import io.github.oshai.kotlinlogging.KotlinLogging

class KotlinLoggingLogger(name: String) : Log {
    private val logger = KotlinLogging.logger(name)

    override fun debug(message: String) {
        logger.debug { message }
    }

    override fun info(message: String) {
        logger.info { message }
    }

    override fun warn(message: String) {
        logger.warn { message }
    }

    override fun error(message: String, throwable: Throwable?) {
        if (throwable != null) {
            logger.error(throwable) { message }
        } else {
            logger.error { message }
        }
    }
}
