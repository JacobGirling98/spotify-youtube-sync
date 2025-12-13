package org.example.domain.music

import arrow.core.Either
import org.example.domain.error.Error
import org.example.domain.model.SongDictionary

interface SongDictionaryRepository {
    fun load(): Either<Error, SongDictionary>
    fun save(dictionary: SongDictionary): Either<Error, Unit>
}
