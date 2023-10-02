package io.hackle.sdk.internal.event

import io.hackle.sdk.HackleConfig
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.CloseableHttpClient
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isIn
import java.net.URI
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

/**
 * @author Yong
 */
@ExtendWith(MockKExtension::class)
internal class EventDispatcherTest {

    @RelaxedMockK
    private lateinit var httpClient: CloseableHttpClient

    @RelaxedMockK
    private lateinit var dispatcherExecutor: ExecutorService

    @BeforeEach
    fun before() {
        every {
            dispatcherExecutor.submit(any())
        } answers {
            firstArg<Runnable>().run()
            mockk()
        }
        mockkStatic("io.hackle.sdk.internal.event.DtoKt")
    }

    @AfterEach
    fun after() {
        unmockkStatic("io.hackle.sdk.internal.event.DtoKt")
    }

    private fun sut(
        config: HackleConfig = HackleConfig.DEFAULT,
        httpClient: CloseableHttpClient = this.httpClient,
        dispatcherExecutor: ExecutorService = this.dispatcherExecutor,
        shutdownTimeoutMillis: Long = 10000
    ): EventDispatcher {
        return EventDispatcher(
            config = config,
            httpClient = httpClient,
            dispatcherExecutor = dispatcherExecutor,
            shutdownTimeoutMillis = shutdownTimeoutMillis
        )
    }

    @DisplayName("dispatch()")
    @Nested
    inner class DispatchTest {

        @Test
        fun `when executor queue is full then do nothing`() {
            // given
            every { dispatcherExecutor.submit(any()) } throws RejectedExecutionException()

            // when
            val sut = sut()
            sut.dispatch(listOf(UserEvents.track("test")))

            // then
            verify { httpClient wasNot Called }
        }


        @Test
        fun `when exception on submitting task then do nothing`() {
            // given
            every { dispatcherExecutor.submit(any()) } throws IllegalArgumentException()

            // when
            val sut = sut()
            sut.dispatch(listOf(UserEvents.track("test")))

            // then
            verify { httpClient wasNot Called }
        }

        @Test
        fun `not successful`() {
            val events = listOf(UserEvents.track("test"))
            every { httpClient.execute(any()) } returns response(500)

            val sut = sut()
            sut.dispatch(events)
        }

        @Test
        fun `successful`() {
            val events = listOf(UserEvents.track("test"))
            every { httpClient.execute(any()) } returns response(202)
            val config = HackleConfig.builder()
                .eventUrl("test_url")
                .build()

            val sut = sut(config = config)
            sut.dispatch(events)

            verify(exactly = 1) {
                httpClient.execute(withArg {
                    expectThat(it) {
                        get { uri } isEqualTo URI("test_url/api/v2/events")
                    }
                })
            }
        }
    }

    @DisplayName("close()")
    @Nested
    inner class CloseTest {

        @Test
        fun `wait remain tasks`() {
            // given
            every {
                httpClient.execute(any())
            } answers {
                Thread.sleep(100)
                response(200)
            }

            val sut = sut(
                dispatcherExecutor = Executors.newFixedThreadPool(1),
                shutdownTimeoutMillis = 1000
            )

            // when
            repeat(5) {
                sut.dispatch(listOf(UserEvents.track("test")))
            }

            val start = System.currentTimeMillis()
            sut.close()
            val end = System.currentTimeMillis()

            // then
            expectThat(end - start).isIn(450L..600L)
            verify(exactly = 5) {
                httpClient.execute(any())
            }
        }

        @Test
        fun `when fail to await executor then shutdown now`() {
            // given
            every {
                httpClient.execute(any())
            } answers {
                Thread.sleep(500)
                response(200)
            }

            val sut = sut(
                dispatcherExecutor = Executors.newFixedThreadPool(1),
                shutdownTimeoutMillis = 200
            )

            // when
            sut.dispatch(listOf(UserEvents.track("test")))
            sut.close()

            // then
            verify(exactly = 1) {
                httpClient.execute(any())
            }
        }

        @Test
        fun `when exception on wait termination then shutdown now`() {
            // given
            every { dispatcherExecutor.awaitTermination(any(), any()) } throws InterruptedException()
            val sut = sut()

            // when
            sut.close()

            // then
            verify(exactly = 1) {
                dispatcherExecutor.shutdownNow()
            }
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

