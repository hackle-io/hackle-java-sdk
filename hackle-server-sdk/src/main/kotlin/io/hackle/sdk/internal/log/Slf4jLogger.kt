package io.hackle.sdk.internal.log

import io.hackle.sdk.core.internal.log.Logger
import org.slf4j.LoggerFactory

/**
 * @author Yong
 */
internal class Slf4jLogger(name: String) : Logger {

    private val log: org.slf4j.Logger = LoggerFactory.getLogger(name)

    override fun debug(msg: () -> String) = if (log.isDebugEnabled) log.debug(msg()) else Unit
    override fun info(msg: () -> String) = if (log.isInfoEnabled) log.info(msg()) else Unit
    override fun warn(msg: () -> String) = if (log.isWarnEnabled) log.warn(msg()) else Unit
    override fun error(msg: () -> String) = if (log.isErrorEnabled) log.error(msg()) else Unit
    override fun error(x: Throwable, msg: () -> String) = if (log.isErrorEnabled) log.error(msg(), x) else Unit

    object Factory : Logger.Factory {
        override fun getLogger(name: String): Logger = Slf4jLogger(name)
    }
}
