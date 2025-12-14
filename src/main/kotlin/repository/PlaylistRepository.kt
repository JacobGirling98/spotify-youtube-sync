package org.example.repository

import com.fasterxml.jackson.module.kotlin.readValue
import org.example.config.CustomJackson
import org.example.domain.model.Playlist
import java.io.File

fun playlistRepository(file: File? = null): JsonFileRepository<List<Playlist>> = JsonFileRepository(
    file = file ?: File("data/playlists.json"),
    serializer = { playlists -> CustomJackson.mapper.writeValueAsString(playlists) },
    deserializer = { jsonString -> CustomJackson.mapper.readValue(jsonString) }
)