package io.hackle.sdk.core.model

import io.hackle.sdk.common.IdentifiersBuilder
import io.hackle.sdk.common.User
import io.hackle.sdk.core.user.IdentifierType
import io.hackle.sdk.core.user.add

data class Identifiers internal constructor(private val identifiers: Map<String, String>) {

    operator fun contains(identifier: Identifier): Boolean {
        return identifier.value == get(identifier.type)
    }

    operator fun get(identifierType: String): String? {
        return identifiers[identifierType]
    }

    operator fun get(identifierType: IdentifierType): String? {
        return identifiers[identifierType.key]
    }

    fun asMap(): Map<String, String> {
        return identifiers
    }
    
    companion object {
        private val EMPTY = Identifiers(emptyMap())

        fun empty(): Identifiers {
            return EMPTY
        }

        fun from(identifiers: Map<String, String>): Identifiers {
            if (identifiers.isEmpty()) {
                return empty()
            }
            return Identifiers(identifiers)
        }

        fun from(user: User): Identifiers {
            val identifiers = IdentifiersBuilder()
                .add(user.identifiers)
                .add(IdentifierType.ID, user.id)
                .add(IdentifierType.USER, user.userId)
                .add(IdentifierType.DEVICE, user.deviceId)
                .build()
            return from(identifiers)
        }
    }
}
