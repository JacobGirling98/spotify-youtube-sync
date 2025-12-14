package org.example.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.example.domain.model.Song
import org.example.domain.model.Name
import org.example.domain.model.Artist

import org.http4k.core.Body
import org.http4k.format.ConfigurableJackson
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import org.http4k.lens.BiDiBodyLens
import org.http4k.format.Jackson.auto

object CustomJackson : ConfigurableJackson(
    KotlinModule.Builder().build()
        .asConfigurable()
        .withStandardMappings()
        .done()
        .deactivateDefaultTyping()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
        .registerModule(SimpleModule().addKeyDeserializer(Song::class.java, SongKeyDeserializer()))
)

class SongKeyDeserializer : KeyDeserializer() {
    override fun deserializeKey(key: String, ctxt: DeserializationContext): Song {
        val parts = key.split(" - ")
        if (parts.size >= 2) {
            val name = Name(parts[0].trim())
            val artistsString = parts.subList(1, parts.size).joinToString(" - ").trim()
            val artists = artistsString.split(",").map { Artist(it.trim()) }
            return Song(name, artists)
        }
        throw IllegalArgumentException("Cannot deserialize Song key: $key")
    }
}

inline fun <reified T : Any> bodyLens(): BiDiBodyLens<T> = Body.auto<T>().toLens()