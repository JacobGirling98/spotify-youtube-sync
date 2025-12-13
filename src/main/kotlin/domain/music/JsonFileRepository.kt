package org.example.domain.music

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.example.domain.error.Error
import org.example.domain.error.JsonError
import org.example.domain.error.NotFoundError
import org.example.repository.Repository
import java.io.File

class JsonFileRepository<T>(
    private val file: File,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T
) : Repository<T> {

    override fun load(): Either<Error, T> {
        if (!file.exists()) return NotFoundError.left()

        return try {
            val fileContent = file.readText()
            deserializer(fileContent).right()
        } catch (e: Exception) {
            JsonError(e.message).left()
        }
    }

    override fun save(data: T): Either<Error, Unit> {
        return try {
            val jsonString = serializer(data)
            file.writeText(jsonString)
            Unit.right()
        } catch (e: Exception) {
            JsonError(e.message).left()
        }
    }
}
