package io.hackle.sdk.core.internal.metrics.delegate

import io.hackle.sdk.core.internal.metrics.Counter
import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.MetricRegistry
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeCounter
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeMetricRegistry
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

internal class AbstractDelegatingMetricTest {

    @DisplayName("add()")
    @Nested
    inner class AddTest {

        @Test
        fun `concurrency read write`() {

            repeat(10) {
                val id = Metric.Id("test", emptyMap(), Metric.Type.COUNTER)
                val noopCounter = mockk<Counter>()
                val sut = DelegatingMetricStub(id, noopCounter)

                val executor = Executors.newFixedThreadPool(32)

                val latch = CountDownLatch(10000)
                repeat(10000) {
                    executor.execute {
                        if (it % 2 == 0) {
                            sut.metrics()
                        } else {
                            sut.add(CumulativeMetricRegistry())
                        }
                        latch.countDown()
                    }
                }
                latch.await()
                expectThat(sut.metrics().size).isEqualTo(5000)
            }
        }

        @Test
        fun `Registry 가 추가된 만큼 Metric 이 생긴다`() {
            // given
            val id = Metric.Id("test", emptyMap(), Metric.Type.COUNTER)
            val noopCounter = mockk<Counter>()
            val sut = DelegatingMetricStub(id, noopCounter)

            // when
            repeat(42) {
                sut.add(CumulativeMetricRegistry())
            }

            // then
            expectThat(sut.metrics()).hasSize(42)
        }
    }

    @DisplayName("first()")
    @Nested
    inner class FirstTest {
        @Test
        fun `추가된 Metric 이 없으면 noopMetric 리턴`() {
            // given
            val id = Metric.Id("test", emptyMap(), Metric.Type.COUNTER)
            val noopCounter = mockk<Counter>()
            val sut = DelegatingMetricStub(id, noopCounter)

            // when
            val actual = sut.first()

            // then
            expectThat(actual).isSameInstanceAs(noopCounter)
        }

        @Test
        fun `추가된 Metric 리턴`() {
            // given
            val id = Metric.Id("test", emptyMap(), Metric.Type.COUNTER)
            val noopCounter = mockk<Counter>()
            val sut = DelegatingMetricStub(id, noopCounter)

            // when
            sut.add(CumulativeMetricRegistry())
            val actual = sut.first()

            // then
            expectThat(actual)
                .isA<CumulativeCounter>()
        }
    }

    class DelegatingMetricStub(
        id: Metric.Id,
        override val noopMetric: Counter
    ) : AbstractDelegatingMetric<Counter>(id), Counter {
        fun metrics() = metrics
        override fun count(): Long = 0
        override fun increment(delta: Long) {}
        override fun registerNewMetric(registry: MetricRegistry): Counter = registry.counter(id)
    }
}