package org.example.domain.model

import arrow.core.Either
import arrow.core.combine
import arrow.core.raise.either
import org.example.domain.error.MergeError


data class ServiceIds(
    val entries: Map<Service, Id>
) {
    constructor(vararg pairs: Pair<Service, Id>) : this(mapOf(*pairs))

    fun mergeWith(other: ServiceIds): Either<MergeError, ServiceIds> = either {
        ServiceIds(entries.combine(other.entries) { first, _ -> first })
    }

    val services = entries.keys
}