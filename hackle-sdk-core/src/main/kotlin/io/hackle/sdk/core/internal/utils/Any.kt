package io.hackle.sdk.core.internal.utils

import io.hackle.sdk.core.HackleCore
import io.hackle.sdk.core.internal.log.Logger

private val log = Logger<HackleCore>()

fun Any.tryClose() {
    if (this is AutoCloseable) {
        try {
            close()
        } catch (e: Exception) {
            log.warn { "Unexpected exception while trying to close $this: $e" }
        }
    }
}

val Any?.safe get() = Unit

inline fun <reified E : Enum<E>> enumValueOfOrNull(name: String): E? {
    return enumValues<E>().find { it.name == name }
}