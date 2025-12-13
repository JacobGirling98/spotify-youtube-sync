package org.example.domain.music

import org.example.config.CustomJackson
import org.example.domain.model.ServiceIds
import org.example.domain.model.Song
import org.example.domain.model.SongDictionary
import java.io.File

class FileSystemSongDictionaryRepository(
    private val file: File = File("song-dictionary.json")
) : SongDictionaryRepository {

    private data class SongDictionaryEntry(val song: Song, val ids: ServiceIds)

    override fun load(): SongDictionary {
        if (!file.exists()) return SongDictionary.empty()

        return try {
            val fileContent = file.readText()
            val entryList: List<SongDictionaryEntry> = CustomJackson.mapper.readValue(
                fileContent,
                object : com.fasterxml.jackson.core.type.TypeReference<List<SongDictionaryEntry>>() {})
            val entries: Map<Song, ServiceIds> = entryList.associate { it.song to it.ids }
            SongDictionary(entries)
        } catch (_: Exception) {
            SongDictionary.empty()
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
