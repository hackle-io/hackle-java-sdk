package io.hackle.sdk.core.internal.log

/**
 * @author Yong
 */
interface Logger {

    fun info(msg: () -> String)
    fun warn(msg: () -> String)
    fun error(msg: () -> String)
    fun error(x: Throwable, msg: () -> String)

    fun interface Factory {
        fun getLogger(name: String): Logger
        fun getLogger(clazz: Class<*>): Logger = getLogger(clazz.name)
    }

    companion object {
        var factory: Factory = Factory { NoopLogger }
        inline operator fun <reified T> invoke(): Logger = factory.getLogger(T::class.java)
    }
}
