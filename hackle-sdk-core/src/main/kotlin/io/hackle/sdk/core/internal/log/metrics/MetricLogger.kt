package io.hackle.sdk.core.internal.log.metrics

import io.hackle.sdk.core.internal.log.LogLevel.*
import io.hackle.sdk.core.internal.log.Logger

internal class MetricLogger(private val counter: LogCounter) : Logger {
    override fun debug(msg: () -> String) = counter.increment(DEBUG)
    override fun info(msg: () -> String) = counter.increment(INFO)
    override fun warn(msg: () -> String) = counter.increment(WARN)
    override fun error(msg: () -> String) = counter.increment(ERROR)
    override fun error(x: Throwable, msg: () -> String) = counter.increment(ERROR)
}
