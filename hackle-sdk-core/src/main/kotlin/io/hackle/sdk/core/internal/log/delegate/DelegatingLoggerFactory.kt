package io.hackle.sdk.core.internal.log.delegate

import io.hackle.sdk.core.internal.log.Logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DelegatingLoggerFactory : Logger.Factory {

    private val lock = Any()

    private val factories = hashSetOf<Logger.Factory>()

    // Use ConcurrentHashMap to avoid ConcurrentModificationException.
    private val _loggers = ConcurrentHashMap<String, DelegatingLogger>()
    internal val loggers: List<DelegatingLogger> get() = Collections.unmodifiableList(ArrayList(_loggers.values))

    override fun getLogger(name: String): Logger {
        return synchronized(lock) {
            // Do NOT use computeIfAbsent to support below Android 24
            _loggers.getOrPut(name) { createLogger(name) }
        }
    }

    private fun createLogger(name: String): DelegatingLogger {
        return DelegatingLogger(name)
            .also { addFactories(it) }
    }

    private fun addFactories(logger: DelegatingLogger) {
        synchronized(lock) {
            for (factory in factories) {
                logger.add(factory)
            }
        }
    }

    internal fun add(factory: Logger.Factory) {
        if (factory is DelegatingLoggerFactory) {
            return
        }
        synchronized(lock) {
            if (factories.add(factory)) {
                for (logger in loggers) {
                    logger.add(factory)
                }
            }
        }
    }
}
