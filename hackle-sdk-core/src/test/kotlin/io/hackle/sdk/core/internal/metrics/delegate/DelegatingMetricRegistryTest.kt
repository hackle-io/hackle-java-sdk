package io.hackle.sdk.core.internal.metrics.delegate

import io.hackle.sdk.core.internal.metrics.MetricRegistry
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeMetricRegistry
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.startsWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

internal class DelegatingMetricRegistryTest {

    @Test
    fun `DelegatingCounter`() {
        val registry = DelegatingMetricRegistry()
        expectThat(registry.counter("counter")).isA<DelegatingCounter>()
    }

    @Test
    fun `DelegatingTimer`() {
        val registry = DelegatingMetricRegistry()
        expectThat(registry.timer("timer")).isA<DelegatingTimer>()
    }

    @DisplayName("add(registry)")
    @Nested
    inner class AddTest {
        @Test
        fun `DelegatingMetricRegistry 는 추가하지 않는다`() {
            val delegating = DelegatingMetricRegistry()

            delegating.add(DelegatingMetricRegistry())
            delegating.add(DelegatingMetricRegistry())
            delegating.add(DelegatingMetricRegistry())
            delegating.add(DelegatingMetricRegistry())
            delegating.add(DelegatingMetricRegistry())
            delegating.add(DelegatingMetricRegistry())

            val counter = delegating.counter("counter")
            counter.increment()

            expectThat(counter.count()).isEqualTo(0)
        }

        @Test
        fun `이미 추가된 Registry 는 추가하지 않는다`() {
            // given
            val delegating = DelegatingMetricRegistry()
            val cumulative = CumulativeMetricRegistry()

            delegating.add(cumulative)
            delegating.add(cumulative)
            delegating.add(cumulative)
            delegating.add(cumulative)

            // when
            delegating.counter("counter").increment()

            // then
            expectThat(cumulative.counter("counter").count()).isEqualTo(1)
        }

        @Test
        fun `metric before registry add`() {
            val delegating = DelegatingMetricRegistry()
            val delegatingCounter = delegating.counter("counter")
            delegatingCounter.increment()

            expectThat(delegatingCounter.count()).isEqualTo(0) // NoopCounter

            val cumulative = CumulativeMetricRegistry()
            delegating.add(cumulative)

            delegatingCounter.increment()

            expectThat(delegatingCounter.count()).isEqualTo(1)
            expectThat(cumulative.counter("counter").count()).isEqualTo(1)
        }

        @Test
        fun `registry before metric add`() {
            // given
            val delegating = DelegatingMetricRegistry()
            val cumulative = CumulativeMetricRegistry()
            delegating.add(cumulative)

            // when
            delegating.counter("counter").increment()

            // then
            expectThat(cumulative.counter("counter").count()).isEqualTo(1)
        }
    }

    @Test
    fun `close`() {
        // given
        val delegating = DelegatingMetricRegistry()
        val registry1 = mockk<MetricRegistry>()
        val registry2 = mockk<MetricRegistry>()
        delegating.add(registry1)
        delegating.add(registry2)

        // when
        delegating.close()

        // then
        verify(exactly = 1) { registry1.close() }
        verify(exactly = 1) { registry2.close() }
    }

    @Test
    fun `Metric 생성 & Registry 등록 동시성`() {
        val executor = Executors.newFixedThreadPool(32)
        repeat(10) {
            val registry = DelegatingMetricRegistry()
            val cumulativeRegistries = List(500) { CumulativeMetricRegistry() }
            val latch = CountDownLatch(1000)
            repeat(1000) {
                executor.execute {
                    if (it % 2 == 0) {
                        registry.counter(it.toString())
                    } else {
                        registry.add(cumulativeRegistries[it / 2])
                    }
                    latch.countDown()
                }
            }
            latch.await()
            expectThat(registry.metrics.size).isEqualTo(500)
            for (i in 0 until 1000 step 2) {
                val name = i.toString()
                registry.counter(name).increment()
                val count = cumulativeRegistries.sumOf { it.counter(name).count() }
                expectThat(count).isEqualTo(500)
            }
        }
    }

    @Test
    fun `toString()`() {
        val delegating = DelegatingMetricRegistry()
        delegating.add(CumulativeMetricRegistry())

        expectThat(delegating.toString())
            .startsWith("DelegatingMetricRegistry")
            .contains("CumulativeMetricRegistry")
    }
}