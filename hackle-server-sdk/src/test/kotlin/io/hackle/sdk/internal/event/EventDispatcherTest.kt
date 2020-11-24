package io.hackle.sdk.internal.event

import io.hackle.sdk.core.event.UserEvent
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.apache.http.impl.client.CloseableHttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.util.concurrent.ExecutorService

/**
 * @author Yong
 */
@ExtendWith(MockKExtension::class)
internal class EventDispatcherTest {


    @MockK
    private lateinit var httpClient: CloseableHttpClient

    @MockK
    private lateinit var dispatcherExecutor: ExecutorService

    private lateinit var sut: EventDispatcher

    @BeforeEach
    fun before() {
        mockkStatic("io.hackle.sdk.internal.event.DtoKt")
    }

    @AfterEach
    fun after() {
        unmockkStatic("io.hackle.sdk.internal.event.DtoKt")
    }

    private fun init(eventBaseUrl: String = "localhost", shutdownTimeout: Duration = Duration.ofSeconds(10)) {
        sut = EventDispatcher(eventBaseUrl, httpClient, dispatcherExecutor, shutdownTimeout)
    }

    @Test
    fun `DispatchTask를 생성후 실행시킨다`() {
        // given
        init()
        val events = mockk<List<UserEvent>>()
        every { events.toPayload() } returns mockk()

        // when
        sut.dispatch(events)

        //then
        verify(exactly = 1) {
            dispatcherExecutor.submit(any())
        }
    }
}
