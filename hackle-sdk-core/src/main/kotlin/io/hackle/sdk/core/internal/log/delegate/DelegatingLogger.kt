package io.hackle.sdk.core.internal.log.delegate

import io.hackle.sdk.core.internal.log.Logger

internal class DelegatingLogger(private val name: String) : Logger {

    private val lock = Any()
    private var loggers = emptyMap<Logger.Factory, Logger>()

    fun add(factory: Logger.Factory) {
        val newLogger = factory.getLogger(name)
        synchronized(lock) {
            val newLoggers = HashMap(loggers)
            newLoggers[factory] = newLogger
            loggers = newLoggers
        }
    }

    override fun debug(msg: () -> String) = loggers.values.forEach { it.debug(msg) }
    override fun info(msg: () -> String) = loggers.values.forEach { it.info(msg) }
    override fun warn(msg: () -> String) = loggers.values.forEach { it.warn(msg) }
    override fun error(msg: () -> String) = loggers.values.forEach { it.error(msg) }
    override fun error(x: Throwable, msg: () -> String) = loggers.values.forEach { it.error(x, msg) }
}
