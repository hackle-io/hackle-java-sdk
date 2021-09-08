package io.hackle.sdk.internal.event

import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.scheduler.ScheduledJob
import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.internal.event.DefaultEventProcessor.Message
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isSameInstanceAs
import java.util.concurrent.*
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * @author Yong
 */
@ExtendWith(MockKExtension::class)
internal class DefaultEventProcessorTest {

    @RelaxedMockK
    private lateinit var queue: BlockingQueue<Message>

    @RelaxedMockK
    private lateinit var eventDispatcher: EventDispatcher

    @RelaxedMockK
    private lateinit var flushScheduler: Scheduler

    @RelaxedMockK
    private lateinit var consumingExecutor: ExecutorService

    private lateinit var sut: DefaultEventProcessor

    private fun setDefaultEventProcessor(
        queue: BlockingQueue<Message>? = null,
        eventDispatchSize: Int = 10,
        flushIntervalMillis: Long = 10 * 1000,
        shutdownTimeoutMillis: Long = 10 * 1000
    ) {
        sut = DefaultEventProcessor(
            queue ?: this.queue,
            eventDispatcher,
            eventDispatchSize,
            flushScheduler,
            flushIntervalMillis,
            consumingExecutor,
            shutdownTimeoutMillis
        )
    }

    @BeforeEach
    fun before() {
        mockkStatic("io.hackle.sdk.core.internal.utils.AnyKt")
    }

    @AfterEach
    fun after() {
        unmockkStatic("io.hackle.sdk.core.internal.utils.AnyKt")
    }

    @Test
    fun `입력한 event를 그대로 큐에 넣는다`() {
        // given
        setDefaultEventProcessor()
        val event = mockk<UserEvent>()
        every { queue.offer(any()) } returns true

        // when
        sut.process(event)

        //then
        verify(exactly = 1) {
            queue.offer(withArg {
                expectThat(it)
                    .isA<Message.Event>()
                    .get { event } isSameInstanceAs event
            })
        }
    }

    @Nested
    inner class Start {

        @Test
        fun `Consuming 을 시작한다`() {
            // given
            setDefaultEventProcessor()

            // when
            sut.start()

            //then
            verify(exactly = 1) { consumingExecutor.submit(any()) }
        }

        @Test
        fun `입력받은 주기로 flush를 실행한다`() {
            // given
            setDefaultEventProcessor(flushIntervalMillis = 320)

            // when
            sut.start()

            //then
            verify(exactly = 1) {
                flushScheduler.schedulePeriodically(
                    delay = 320,
                    period = 320,
                    unit = MILLISECONDS,
                    task = any()
                )
            }
        }

        @Test
        fun `Flush가 되는 경우 Message_Flush를 큐에 넣는다`() {
            // given
            setDefaultEventProcessor()
            val flushTask = slot<() -> Unit>()

            // when
            sut.start()

            //then
            verify(exactly = 1) {
                flushScheduler.schedulePeriodically(any(), any(), any(), capture(flushTask))
            }

            flushTask.captured()

            verify(exactly = 1) {
                queue.offer(withArg { expectThat(it).isA<Message.Flush>() })
            }
        }

        @Test
        fun `한번만 시작 할 수 있다`() {
            // given
            setDefaultEventProcessor()

            // when
            sut.start()
            sut.start()

            //then
            verify(exactly = 1) { consumingExecutor.submit(any()) }
            verify(exactly = 1) { flushScheduler.schedulePeriodically(any(), any(), any(), any()) }
        }
    }

    @Nested
    inner class Close {

        @Test
        fun `시작이 안되었으면 바로 종료한다`() {
            // given
            setDefaultEventProcessor()

            // when
            sut.close()

            //then
            verify { flushScheduler wasNot Called }
            verify { consumingExecutor wasNot Called }
            verify { eventDispatcher wasNot Called }
        }

        @Test
        fun `flushingJob을 취소시킨다`() {
            // given
            setDefaultEventProcessor()
            val flushingJob = mockk<ScheduledJob>(relaxed = true)
            every { flushScheduler.schedulePeriodically(any(), any(), any(), any()) } returns flushingJob

            // when
            sut.start()
            sut.close()

            //then
            verify(exactly = 1) { flushingJob.cancel() }
        }

        @Test
        fun `flushScheduler를 종료시킨다`() {
            // given
            setDefaultEventProcessor()

            mockkStatic("io.hackle.sdk.core.internal.utils.AnyKt")

            // when
            sut.start()
            sut.close()

            //then
            verify(exactly = 1) { flushScheduler.tryClose() }
        }

        @Test
        fun `큐에 Shutdown Message를 넣는다`() {
            // given
            setDefaultEventProcessor(shutdownTimeoutMillis = 320)

            // when
            sut.start()
            sut.close()

            //then
            verify(exactly = 1) {
                queue.offer(Message.Shutdown, 320, MILLISECONDS)
            }
        }

        @Test
        fun `큐에 Shutdown Message가 정상적으로 들어갔으면 ConsumingTask가 끝날때까지 기다린다`() {
            // given
            setDefaultEventProcessor(shutdownTimeoutMillis = 42)

            val consumingTask = mockk<Future<*>>(relaxed = true)
            every { consumingExecutor.submit(any()) } returns consumingTask

            every { queue.offer(any(), any(), any()) } returns true

            // when
            sut.start()
            sut.close()

            //then
            verify(exactly = 1) {
                consumingTask.get(42, MILLISECONDS)
            }
        }

        @Test
        fun `ConsumingTask 기다리는 동안 타임아웃이 발생하면 무시하고 종료한다`() {
            // given
            setDefaultEventProcessor(shutdownTimeoutMillis = 42)

            val consumingTask: Future<*> = CompletableFuture.runAsync { Thread.sleep(100) }
            every { consumingExecutor.submit(any()) } returns consumingTask

            every { queue.offer(any(), any(), any()) } returns true

            // when
            sut.start()
            sut.close()

            //then
            verify(exactly = 1) { consumingExecutor.shutdownNow() }
        }

        @Test
        fun `큐에 Shutdown Message가 정삭적으로 들어가지 못했으면 ConsumingTask를 기다리지 않는다`() {
            // given
            setDefaultEventProcessor(shutdownTimeoutMillis = 42)

            val consumingTask = mockk<Future<*>>()
            every { consumingExecutor.submit(any()) } returns consumingTask

            every { queue.offer(any(), any(), any()) } returns false

            // when
            sut.start()
            sut.close()

            //then
            verify { consumingTask wasNot Called }
        }

        @Test
        fun `consumingExecutor를 Shutdown시킨다`() {
            // given
            setDefaultEventProcessor()

            // when
            sut.start()
            sut.close()

            //then
            verify(exactly = 1) { consumingExecutor.shutdownNow() }
        }

        @Test
        fun `eventDispatcher를 종료시킨다`() {
            // given
            setDefaultEventProcessor()

            // when
            sut.start()
            sut.close()

            //then
            verify(exactly = 1) { eventDispatcher.tryClose() }
        }
    }
}
