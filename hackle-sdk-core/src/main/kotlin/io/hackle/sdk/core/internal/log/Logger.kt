package io.hackle.sdk.core.internal.log

import io.hackle.sdk.core.internal.log.delegate.DelegatingLoggerFactory

/**
 * @author Yong
 */
interface Logger {

    fun debug(msg: () -> String)
    fun info(msg: () -> String)
    fun warn(msg: () -> String)
    fun error(msg: () -> String)
    fun error(x: Throwable, msg: () -> String)

    fun interface Factory {
        fun getLogger(name: String): Logger
    }

    companion object {

        val factory = DelegatingLoggerFactory()

        fun add(factory: Factory) {
            this.factory.add(factory)
        }

        inline operator fun <reified T> invoke(): Logger = factory.getLogger(T::class.java.name)
        operator fun invoke(name: String): Logger = factory.getLogger(name)
    }
}
