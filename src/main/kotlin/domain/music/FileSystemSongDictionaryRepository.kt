package org.example.domain.music

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.example.config.CustomJackson
import org.example.domain.error.Error
import org.example.domain.error.JsonError
import org.example.domain.error.NotFoundError
import org.example.domain.model.ServiceIds
import org.example.domain.model.Song
import org.example.domain.model.SongDictionary
import java.io.File

class FileSystemSongDictionaryRepository(
    private val file: File = File("song-dictionary.json")
) : SongDictionaryRepository {

    private data class SongDictionaryEntry(val song: Song, val ids: ServiceIds)

    override fun load(): Either<Error, SongDictionary> {
        if (!file.exists()) return NotFoundError.left()

        return try {
            val fileContent = file.readText()
            val entryList: List<SongDictionaryEntry> = CustomJackson.mapper.readValue(
                fileContent,
                object : com.fasterxml.jackson.core.type.TypeReference<List<SongDictionaryEntry>>() {})
            val entries: Map<Song, ServiceIds> = entryList.associate { it.song to it.ids }
            SongDictionary(entries).right()
        } catch (e: Exception) {
            JsonError(e.message).left()
        }
    }

    override fun save(dictionary: SongDictionary) {
        try {
            val entryList = dictionary.entries.map { (song, ids) -> SongDictionaryEntry(song, ids) }
            val jsonString = CustomJackson.asFormatString(entryList)
            file.writeText(jsonString)
        } catch (e: Exception) {
            println("Error saving song dictionary cache. Error: ${e.message}")
        }
    }
}
