package org.example.config


import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.example.config.CustomJackson.auto
import org.http4k.core.Body
import org.http4k.format.ConfigurableJackson
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import org.http4k.lens.BiDiBodyLens

object CustomJackson : ConfigurableJackson(
    KotlinModule.Builder().build()
        .asConfigurable()
        .withStandardMappings()
        .done()
        .deactivateDefaultTyping()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
)

inline fun <reified T : Any> bodyLens(): BiDiBodyLens<T> = Body.auto<T>().toLens()