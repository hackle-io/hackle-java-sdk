package io.hackle.sdk.core.internal.utils

import io.hackle.sdk.core.client.HackleClient
import io.hackle.sdk.core.internal.log.Logger

private val log = Logger<HackleClient>()

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
