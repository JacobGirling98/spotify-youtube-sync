package org.example.domain.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class Name(val value: String) {
    private val normalizedValue: String = value.replace("'", "").lowercase()

    @JsonValue
    fun toJson(): String = value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Name

        return normalizedValue == other.normalizedValue
    }

    override fun hashCode(): Int {
        return normalizedValue.hashCode()
    }

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromJson(value: String): Name = Name(value)
    }
}

data class Artist(val value: String) {
    override fun equals(other: Any?): Boolean = other is Artist && value.equals(other.value, ignoreCase = true)
    override fun hashCode(): Int = value.lowercase().hashCode()

    @JsonValue
    fun toJson(): String = value

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromJson(value: String): Artist = Artist(value)
    }
}

@JvmInline value class Id(val value: String)
