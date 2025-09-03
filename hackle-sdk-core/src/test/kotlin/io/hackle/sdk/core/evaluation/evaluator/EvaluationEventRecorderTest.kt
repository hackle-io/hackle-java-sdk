package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEventFactory
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class EvaluationEventRecorderTest {

    @MockK
    private lateinit var eventFactory: UserEventFactory

    @RelaxedMockK
    private lateinit var eventProcessor: EventProcessor

    @InjectMockKs
    private lateinit var sut: EvaluationEventRecorder

    @Test
    fun `record`() {
        // given
        every { eventFactory.create(any(), any()) } returns listOf(mockk(), mockk())

        // when
        sut.record(mockk(), mockk())

        // then
        verify(exactly = 2) {
            eventProcessor.process(any())
        }
    }
}
