package io.hackle.sdk.core.internal.log

/**
 * @author Yong
 */
internal object NoopLogger : Logger {
    override fun debug(msg: () -> String) {}
    override fun info(msg: () -> String) {}
    override fun warn(msg: () -> String) {}
    override fun error(msg: () -> String) {}
    override fun error(x: Throwable, msg: () -> String) {}

    class Factory : Logger.Factory {
        override fun getLogger(name: String): Logger = NoopLogger
    }
}
