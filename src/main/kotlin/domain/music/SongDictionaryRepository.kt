package org.example.domain.music

import org.example.domain.model.SongDictionary

interface SongDictionaryRepository {
    fun load(): SongDictionary
    fun save(dictionary: SongDictionary)
}
