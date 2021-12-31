package io.hackle.sdk.core.model

import java.util.*
import java.util.regex.Pattern

internal class Version private constructor(
    val coreVersion: CoreVersion,
    val prerelease: MetadataVersion,
    val build: MetadataVersion,
) : Comparable<Version> {

    val plainString: String
        get() {
            return buildString {
                append(coreVersion)
                if (prerelease.isNotEmpty) {
                    append("-")
                    append(prerelease)
                }
                if (build.isNotEmpty) {
                    append("+")
                    append(build)
                }
            }
        }

    override fun compareTo(other: Version): Int {
        val result = this.coreVersion.compareTo(other.coreVersion)
        if (result != 0) {
            return result
        }
        return this.prerelease.compareTo(other.prerelease)
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is Version -> false
            else -> this.compareTo(other) == 0
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(coreVersion, prerelease)
    }

    override fun toString(): String {
        return "Version($plainString)"
    }

    companion object {

        private val PATTERN: Pattern =
            Pattern.compile(
                "^(0|[1-9]\\d*)" +
                    "(?:\\.(0|[1-9]\\d*))?" +
                    "(?:\\.(0|[1-9]\\d*))?" +
                    "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
                    "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\$"
            )

        fun parseOrNull(version: String): Version? {
            val matcher = PATTERN.matcher(version)
            if (!matcher.matches()) {
                return null
            }

            val major: Int = matcher.group(1).toInt()
            val minor: Int = matcher.group(2)?.toInt() ?: 0
            val patch: Int = matcher.group(3)?.toInt() ?: 0

            val coreVersion = CoreVersion(major, minor, patch)
            val prerelease = MetadataVersion.parse(matcher.group(4))
            val build = MetadataVersion.parse(matcher.group(5))

            return Version(coreVersion, prerelease, build)
        }

        fun parseOrNull(value: Any): Version? {
            return when (value) {
                is Version -> value
                !is String -> null
                else -> parseOrNull(value)
            }
        }
    }
}

internal data class CoreVersion(
    private val major: Int,
    private val minor: Int,
    private val patch: Int,
) : Comparable<CoreVersion> {
    override fun compareTo(other: CoreVersion): Int {
        val majorDiff = this.major.compareTo(other.major)
        if (majorDiff != 0) {
            return majorDiff
        }

        val minorDiff = this.minor.compareTo(other.minor)
        if (minorDiff != 0) {
            return minorDiff
        }

        return this.patch.compareTo(other.patch)
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }
}

internal data class MetadataVersion(private val identifiers: List<String>) : Comparable<MetadataVersion> {

    val isEmpty: Boolean get() = identifiers.isEmpty()
    val isNotEmpty: Boolean get() = !isEmpty

    override fun compareTo(other: MetadataVersion): Int {
        return when {
            this.isEmpty && other.isEmpty -> 0
            this.isEmpty && other.isNotEmpty -> 1
            this.isNotEmpty && other.isEmpty -> -1
            else -> compareIdentifiers(other)
        }
    }

    private fun compareIdentifiers(other: MetadataVersion): Int {
        for ((thisIdentifier, otherIdentifier) in this.identifiers.zip(other.identifiers)) {
            val result = compareIdentifiers(thisIdentifier, otherIdentifier)
            if (result != 0) {
                return result
            }
        }
        return this.identifiers.size.compareTo(other.identifiers.size)
    }

    private fun compareIdentifiers(identifier1: String, identifier2: String): Int {
        val number1 = identifier1.toIntOrNull()
        val number2 = identifier2.toIntOrNull()
        return if (number1 != null && number2 != null) {
            number1.compareTo(number2)
        } else {
            identifier1.compareTo(identifier2)
        }
    }

    override fun toString(): String = identifiers.joinToString(".")

    companion object {
        private val EMPTY = MetadataVersion(emptyList())
        fun parse(value: String?): MetadataVersion {
            return if (value == null) {
                EMPTY
            } else {
                MetadataVersion(value.split("."))
            }
        }
    }
}
