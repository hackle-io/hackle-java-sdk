package io.hackle.sdk.core.internal.metrics

import java.util.*

interface Metric {

    val id: Id

    fun measure(): List<Measurement>

    enum class Type {
        COUNTER, TIMER
    }

    class Id(
        val name: String,
        val tags: Map<String, String>,
        val type: Type,
    ) {

        override fun equals(other: Any?): Boolean {
            return when {
                this === other -> true
                other !is Id -> false
                else -> name == other.name && tags == other.tags
            }
        }

        override fun hashCode(): Int {
            return Objects.hash(name, tags)
        }

        override fun toString(): String {
            return "MetricId(name='$name', tags=$tags)"
        }
    }

    abstract class Builder<out M : Metric>(
        private val name: String,
        private val type: Type,
        private val register: (registry: MetricRegistry, id: Id) -> M
    ) {
        private val tags = hashMapOf<String, String>()

        fun tags(tags: Map<String, String>) = apply { this.tags += tags }
        fun tags(vararg tags: Pair<String, String>) = apply { this.tags += tags }
        fun tag(key: String, value: String) = apply { this.tags[key] = value }

        fun register(registry: MetricRegistry): M {
            val id = Id(name, Collections.unmodifiableMap(tags), type)
            return register(registry, id)
        }
    }
}
