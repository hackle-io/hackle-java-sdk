package io.hackle.sdk.core


object HackleCoreContext {
    private val instanceMap: LinkedHashMap<String, Any> = linkedMapOf()

    fun <T> get(requiredType: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return instanceMap.values.find { requiredType.isInstance(it) } as T
    }

    fun <T> getOrNull(requiredType: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return instanceMap.values.find { requiredType.isInstance(it) } as? T
    }

    fun registerInstance(instance: Any) {
        val name = instance::class.java.simpleName
        if (!instanceMap.contains(name)) {
            this.instanceMap[name] = instance
        }
    }

    fun getInstanceMapSize(): Int {
        return instanceMap.size
    }

    fun clear() {
        instanceMap.clear()
    }
}
