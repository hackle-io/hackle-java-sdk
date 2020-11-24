package io.hackle.sdk.core.internal.utils

import java.time.Duration

val Duration.millis: Long get() = toMillis()
val Int.seconds: Duration get() = Duration.ofSeconds(this.toLong())
val Int.minutes: Duration get() = Duration.ofMinutes(this.toLong())

private val FORMAT_REGEX = "(\\d[HMS])(?!$)".toRegex()
fun Duration.format(): String {
    return toString()
        .substring(2)
        .replace(FORMAT_REGEX, "$1 ")
        .toLowerCase()
}
