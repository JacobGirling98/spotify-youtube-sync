package unit.domain.music

import arrow.core.left
import arrow.core.right
import com.fasterxml.jackson.core.type.TypeReference
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.example.config.CustomJackson
import org.example.domain.error.JsonError
import org.example.domain.error.NotFoundError
import org.example.domain.music.JsonFileRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.util.UUID

class JsonFileRepositoryTest {

    private lateinit var tempFile: File
    private lateinit var repository: JsonFileRepository<TestDataType>

    // A simple data class to test generic serialization/deserialization
    private data class TestDataType(val message: String, val value: Int)

    @BeforeEach
    fun setUp() {
        tempFile = Files.createTempFile("test-json-repo-${UUID.randomUUID()}", ".json").toFile()
        // Instantiate JsonFileRepository with serializers/deserializers for TestDataType
        repository = JsonFileRepository(
            tempFile,
            { data -> CustomJackson.asFormatString(data) },
            { jsonString -> CustomJackson.mapper.readValue(jsonString, TestDataType::class.java) }
        )
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
    fun `should save and load data correctly`() {
        // Arrange
        val testData = TestDataType("Hello, World!", 123)

        // Act
        val saveResult = repository.save(testData)
        saveResult shouldBe Unit.right()

        val loadedData = repository.load()

        // Assert
        loadedData shouldBe testData.right()
    }

    @Test
    fun `should return a json error when the file is corrupt`() {
        // Arrange
        tempFile.writeText("this is not valid json")

        // Act
        val result = repository.load()

        // Assert
        result.isLeft() shouldBe true
        result.leftOrNull().shouldBeInstanceOf<JsonError>()
    }

    @Test
    fun `should overwrite an existing file when saving`() {
        // Arrange
        val initialData = TestDataType("Initial", 1)
        val newData = TestDataType("New", 2)

        // Act
        repository.save(initialData) shouldBe Unit.right()
        repository.save(newData) shouldBe Unit.right()

        val loadedData = repository.load()

        // Assert
        loadedData shouldBe newData.right()
    }

    @Test
    fun `should return a json error when saving to a read-only file`() {
        // Arrange
        val testData = TestDataType("Read Only Test", 99)
        tempFile.setReadOnly()

        // Act
        val saveResult = repository.save(testData)

        // Assert
        saveResult.isLeft() shouldBe true
        saveResult.leftOrNull().shouldBeInstanceOf<JsonError>()
    }
}