package io.hackle.sdk.internal.monitoring.metrics

import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.time.Clock
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@ExtendWith(MockKExtension::class)
internal class MonitoringMetricRegistryTest {

    @RelaxedMockK
    private lateinit var scheduler: Scheduler

    @RelaxedMockK
    private lateinit var httpClient: CloseableHttpClient

    private fun registry(): MonitoringMetricRegistry {
        return MonitoringMetricRegistry(
            monitoringBaseUrl = "http://localhost",
            scheduler = scheduler,
            flushIntervalMillis = 10000,
            httpClient = httpClient,
            clock = Clock.SYSTEM
        )
    }

    @Test
    fun `측정된 counter 는 서버로 전송한다`() {
        // given
        every { httpClient.execute(any<HttpPost>()) } returns response(200)
        val registry = registry()
        registry.counter("test.counter").increment()

        // when
        registry.publish()

        // then
        verify(exactly = 1) { httpClient.execute(any<HttpPost>()) }
    }

    @Test
    fun `측정된 timer 는 서버로 전송한다`() {
        // given
        every { httpClient.execute(any<HttpPost>()) } returns response(200)
        val registry = registry()
        registry.timer("test.timer").record(100, TimeUnit.MILLISECONDS)

        // when
        registry.publish()

        // then
        verify(exactly = 1) { httpClient.execute(any<HttpPost>()) }
    }

    @Test
    fun `측정되지 않은(count 0) metric 은 전송 대상에서 제외된다`() {
        // given
        val registry = registry()
        registry.counter("test.counter") // increment 하지 않음 -> count 0
        registry.timer("test.timer")      // record 하지 않음 -> count 0

        // when
        registry.publish()

        // then: isDispatchTarget 필터로 전송 대상이 없어 전송하지 않는다
        verify(exactly = 0) { httpClient.execute(any<HttpPost>()) }
    }

    @Test
    fun `전송 응답이 실패해도 예외를 전파하지 않는다`() {
        // given
        every { httpClient.execute(any<HttpPost>()) } returns response(500)
        val registry = registry()
        registry.counter("test.counter").increment()

        // when (예외가 전파되면 테스트 실패)
        registry.publish()

        // then
        verify(exactly = 1) { httpClient.execute(any<HttpPost>()) }
    }

    private fun response(code: Int): CloseableHttpResponse {
        return mockk(relaxed = true) {
            every { statusLine } returns mockk {
                every { statusCode } returns code
            }
        }
    }
}
