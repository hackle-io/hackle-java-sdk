package io.hackle.sdk.core.internal.metrics.noop

import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.Metric.Id
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.single

internal class NoopCounterTest {

    @Test
    fun `always zero`() {
        val counter = NoopCounter(Id("counter", emptyMap(), Metric.Type.COUNTER))
        expectThat(counter.count()).isEqualTo(0)
        val measurements = counter.measure()
        expectThat(measurements).single().get { value }.isEqualTo(0.0)
        counter.increment()
        expectThat(counter.count()).isEqualTo(0)
        expectThat(measurements).single().get { value }.isEqualTo(0.0)
    }
}