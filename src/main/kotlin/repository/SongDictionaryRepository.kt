package org.example.repository

import org.example.config.bodyLens
import org.example.domain.model.SongDictionary
import org.http4k.core.Method
import org.http4k.core.Request
import java.io.File

fun songDictionaryRepository(): JsonFileRepository<SongDictionary> {
    val songDictionaryFile = File("data/songDictionary.json")
    val songDictionaryLens = bodyLens<SongDictionary>()
    return JsonFileRepository(
        file = songDictionaryFile,
        serializer = { songDictionary ->
            songDictionaryLens.inject(songDictionary, Request.Companion(Method.GET, "/")).bodyString()
        },
        deserializer = { jsonString -> songDictionaryLens.extract(Request.Companion(Method.GET, "/").body(jsonString)) }
    )
}