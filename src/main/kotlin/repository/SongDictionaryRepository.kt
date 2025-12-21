package org.example.repository

import com.fasterxml.jackson.module.kotlin.readValue
import org.example.config.CustomJackson
import org.example.domain.model.SongDictionary
import java.io.File

fun songDictionaryRepository(file: File? = null): JsonFileRepository<SongDictionary> = JsonFileRepository(
    file = file ?: File("data/songDictionary.json"),
    serializer = { songDictionaries ->
        CustomJackson.mapper.writeValueAsString(songDictionaries)
    },
    deserializer = { jsonString -> CustomJackson.mapper.readValue(jsonString) }
)