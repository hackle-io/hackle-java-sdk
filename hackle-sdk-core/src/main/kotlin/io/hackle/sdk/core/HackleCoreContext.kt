package io.hackle.sdk.core

class HackleCoreContext private constructor() {
    private val instances = mutableListOf<Any>()

    fun <T : Any> register(instance: T): T {
        instances.add(instance)
        return instance
    }

    fun <T : Any> getOrNull(type: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return instances.find { type.isInstance(it) } as? T
    }

    operator fun <T : Any> get(type: Class<T>): T {
        return requireNotNull(getOrNull(type)) { "Instance not registered [${type.simpleName}]" }
    }

    companion object {
        val GLOBAL = HackleCoreContext()
    }
}

inline fun <reified T : Any> HackleCoreContext.get(): T {
    return this[T::class.java]
}

inline fun <reified T : Any> HackleCoreContext.getOrNull(): T? {
    return getOrNull(T::class.java)
}
