package io.hackle.sdk.core.model

import java.util.*

interface Entity {
    val serviceType: ServiceType
    val id: Long
}

abstract class AbstractEntity : Entity {
    override fun toString(): String {
        return "Entity(serviceType=$serviceType, id=$id)"
    }

    final override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is Entity -> false
            else -> this.serviceType == other.serviceType && this.id == other.id
        }
    }

    final override fun hashCode(): Int {
        return Objects.hash(serviceType, id)
    }
}

class DefaultEntity(
    override val serviceType: ServiceType,
    override val id: Long,
) : AbstractEntity()
