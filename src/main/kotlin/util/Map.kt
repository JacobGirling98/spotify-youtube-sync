package org.example.util

fun <K, A> Map<K, A>.combine(
    other: Map<K, A>,
    merge: (K, A, A) -> A
): Map<K, A> {
    val newMap = mutableMapOf<K, A>()
    newMap.putAll(this)
    other.forEach { key, value ->
        val existingValue = newMap[key]
        if (existingValue == null) newMap[key] = value
        else newMap[key] = merge(key, existingValue, value)
    }
    return newMap.toMap()
}