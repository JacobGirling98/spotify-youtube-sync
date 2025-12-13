package unit.domain.music

import arrow.core.left
import fixtures.data.artist
import fixtures.data.serviceIds
import fixtures.data.song
import io.kotest.matchers.shouldBe
import org.example.domain.error.JsonError
import org.example.domain.error.NotFoundError
import org.example.domain.model.Id
import org.example.domain.model.Service
import org.example.domain.model.SongDictionary
import org.example.domain.music.FileSystemSongDictionaryRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.util.UUID
import arrow.core.right
import io.kotest.matchers.types.shouldBeInstanceOf

class FileSystemSongDictionaryRepositoryTest {

    private lateinit var tempFile: File
    private lateinit var repository: FileSystemSongDictionaryRepository

    @BeforeEach
    fun setUp() {
        tempFile = Files.createTempFile("test-${UUID.randomUUID()}", ".json").toFile()
        repository = FileSystemSongDictionaryRepository(tempFile)
    }

    @AfterEach
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `should return not found error when the file does not exist`() {
        // Arrange
        tempFile.delete() // Ensure it doesn't exist for this specific test

        // Act
        val result = repository.load()

        // Assert
        result shouldBe NotFoundError.left()
    }

    @Test
    fun `should save and load a dictionary correctly`() {
        // Arrange
        val song1 = song("Song One", listOf(artist("Artist A")))
        val song2 = song("Song Two", listOf(artist("Artist B")))
        val dictionary = SongDictionary(
            song1 to serviceIds(Service.SPOTIFY, Id("spotify123")),
            song2 to serviceIds(Service.YOUTUBE_MUSIC, Id("youtube456"))
        )

        repository.save(dictionary)

        val result = repository.load()
        result shouldBe dictionary.right()
    }

    @Test
    fun `should return a json error when the file is corrupt`() {
        tempFile.writeText("this is not valid json")

        val result = repository.load()

        result.isLeft() shouldBe true
        result.leftOrNull().shouldBeInstanceOf<JsonError>()
    }

    @Test
    fun `should overwrite an existing file when saving`() {
        val initialDictionary = SongDictionary(song("Initial") to serviceIds(Service.SPOTIFY, Id("initial")))
        val newDictionary = SongDictionary(song("New") to serviceIds(Service.YOUTUBE_MUSIC, Id("new")))

        repository.save(initialDictionary)
        repository.save(newDictionary)

        val result = repository.load()

        result shouldBe newDictionary.right()
    }
}