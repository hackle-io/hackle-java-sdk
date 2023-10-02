package io.hackle.sdk.internal.monitoring.metrics

import io.hackle.sdk.core.internal.metrics.Metrics
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeMetricRegistry
import io.mockk.every
import io.mockk.mockk
import org.apache.http.client.methods.CloseableHttpResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ApiCallMetricsTest {


    @DisplayName("record()")
    @Nested
    inner class RecordTest {

        @Test
        fun `exception`() {
            val registry = CumulativeMetricRegistry()
            Metrics.addRegistry(registry)

            assertThrows<IllegalArgumentException> {
                ApiCallMetrics.record("test_call") { throw IllegalArgumentException() }
            }

            val timer = registry.timer("api.call", mapOf("operation" to "test_call", "success" to "false"))
            expectThat(timer.count()) isEqualTo 1
        }

        @Test
        fun `2xx successful`() {
            val registry = CumulativeMetricRegistry()
            Metrics.addRegistry(registry)

            ApiCallMetrics.record("test_call") { response(200) }

            val timer = registry.timer("api.call", mapOf("operation" to "test_call", "success" to "true"))
            expectThat(timer.count()) isEqualTo 1
        }

        @Test
        fun `304 not modified`() {
            val registry = CumulativeMetricRegistry()
            Metrics.addRegistry(registry)

            ApiCallMetrics.record("test_call") { response(200) }

            val timer = registry.timer("api.call", mapOf("operation" to "test_call", "success" to "true"))
            expectThat(timer.count()) isEqualTo 1
        }

        @Test
        fun `not successful`() {
            val registry = CumulativeMetricRegistry()
            Metrics.addRegistry(registry)

            ApiCallMetrics.record("test_call") { response(500) }

            val timer = registry.timer("api.call", mapOf("operation" to "test_call", "success" to "false"))
            expectThat(timer.count()) isEqualTo 1
        }
    }

    private fun response(code: Int): CloseableHttpResponse {
        return mockk(relaxed = true) {
            every { statusLine } returns mockk {
                every { statusCode } returns code
            }
        }
    }
}