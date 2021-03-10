package io.hackle.sdk.internal.event

import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.scheduler.ScheduledJob
import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.utils.tryClose
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * @author Yong
 */
internal class DefaultEventProcessor(
    private val queue: BlockingQueue<Message>,
    private val eventDispatcher: EventDispatcher,
    private val eventDispatchSize: Int,
    private val flushScheduler: Scheduler,
    private val flushIntervalMillis: Long,
    private val consumingExecutor: ExecutorService,
    private val shutdownTimeoutMillis: Long
) : EventProcessor, AutoCloseable {

    private var flushingJob: ScheduledJob? = null
    private var consumingTask: Future<*>? = null
    private var isStarted: Boolean = false

    override fun process(event: UserEvent) {
        val isProcessed = produce(Message.Event(event))
        if (!isProcessed) {
            log.warn { "Event not processed. Exceeded event queue capacity" }
        }
    }

    private fun flush() {
        produce(Message.Flush)
    }

    private fun produce(message: Message, waitMillis: Long? = null): Boolean {
        return if (waitMillis != null) {
            queue.offer(message, waitMillis, MILLISECONDS)
        } else {
            queue.offer(message)
        }
    }

    fun start() {
        if (isStarted) {
            log.info { "DefaultEventProcessor is already started" }
            return
        }

        consumingTask = consumingExecutor.submit(Consumer())
        flushingJob =
            flushScheduler.schedulePeriodically(flushIntervalMillis, flushIntervalMillis, MILLISECONDS) { flush() }

        isStarted = true
        log.info { "DefaultEventProcessor started. Flush events every $flushIntervalMillis ms." }
    }

    override fun close() {
        if (!isStarted) {
            return
        }

        log.info { "Shutting down DefaultEventProcessor" }

        flushingJob?.cancel()
        flushScheduler.tryClose()

        try {
            if (produce(Message.Shutdown, shutdownTimeoutMillis)) {
                consumingTask?.awaitShutdown(shutdownTimeoutMillis)
            } else {
                log.error { "Failed to produce shutdown signal to consumer." }
            }
        } catch (e: Exception) {
            log.error { "Failed to process remaining events: $e" }
        } finally {
            consumingExecutor.shutdownNow()
        }

        eventDispatcher.tryClose()
    }

    private fun Future<*>.awaitShutdown(timeoutMillis: Long) {
        get(timeoutMillis, MILLISECONDS)
    }

    sealed class Message {
        class Event(val event: UserEvent) : Message()
        object Flush : Message()
        object Shutdown : Message()
    }

    private inner class Consumer : Runnable {

        private var consumedEvents = mutableListOf<UserEvent>()

        override fun run() {
            consumingLoop()
        }

        private fun consumingLoop() {
            try {
                while (true) {
                    when (val message = queue.take()) {
                        is Message.Event -> consumeEvent(message.event)
                        Message.Flush -> dispatchEvents()
                        Message.Shutdown -> break
                    }
                }
            } catch (e: Exception) {
                log.error { "Unexpected exception in consuming loop: $e" }
            } finally {
                dispatchEvents()
            }
        }

        private fun consumeEvent(userEvent: UserEvent) {
            consumedEvents.add(userEvent)
            if (consumedEvents.size >= eventDispatchSize) {
                dispatchEvents()
            }
        }

        private fun dispatchEvents() {
            if (consumedEvents.isEmpty()) {
                return
            }

            try {
                eventDispatcher.dispatch(consumedEvents)
            } catch (e: Exception) {
                log.error { "Failed to dispatch events: $e" }
            }
            consumedEvents = mutableListOf()
        }
    }

    companion object {
        private val log = Logger<DefaultEventProcessor>()
    }
}
