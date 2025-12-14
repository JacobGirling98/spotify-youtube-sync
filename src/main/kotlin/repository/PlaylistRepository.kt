package org.example.repository

import org.example.config.bodyLens
import org.example.domain.model.Playlist
import org.http4k.core.Method
import org.http4k.core.Request
import java.io.File

fun playlistRepository(): JsonFileRepository<List<Playlist>> {
    val playlistFile = File("data/playlists.json")
    val playlistLens = bodyLens<List<Playlist>>()
    return JsonFileRepository(
        file = playlistFile,
        serializer = { playlists -> playlistLens.inject(playlists, Request.Companion(Method.GET, "/")).bodyString() },
        deserializer = { jsonString -> playlistLens.extract(Request.Companion(Method.GET, "/").body(jsonString)) }
    )
}