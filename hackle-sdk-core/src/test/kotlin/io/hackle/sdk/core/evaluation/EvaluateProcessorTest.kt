package io.hackle.sdk.core.evaluation

import io.hackle.sdk.core.evaluation.evaluator.EvaluatorFactory
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateRequest
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResponse
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateResponse
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateResponse
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateRequest
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResponse
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluator
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.InAppMessages
import io.hackle.sdk.core.support.RemoteConfigs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class EvaluateProcessorTest {

    @MockK
    private lateinit var evaluatorFactory: EvaluatorFactory

    @InjectMockKs
    private lateinit var sut: EvaluateProcessor

    @Nested
    inner class ExperimentTest {

        @Test
        fun `factory 에서 찾은 evaluator 로 평가하고 기록한다`() {
            // given
            val request = Experiments.localRequest()
            val response = mockk<ExperimentEvaluateResponse>()
            val evaluator = mockk<ExperimentEvaluator<ExperimentEvaluateRequest>> {
                every { evaluate(request, any()) } returns response
                justRun { record(request, response) }
            }
            every { evaluatorFactory.experiment(request) } returns evaluator

            // when
            val actual = sut.experiment(request)

            // then
            expectThat(actual) isSameInstanceAs response
            verify(exactly = 1) { evaluator.record(request, response) }
        }

        @Test
        fun `record 가 false 면 기록하지 않는다`() {
            // given
            val request = Experiments.localRequest(record = false)
            val response = mockk<ExperimentEvaluateResponse>()
            val evaluator = mockk<ExperimentEvaluator<ExperimentEvaluateRequest>> {
                every { evaluate(request, any()) } returns response
            }
            every { evaluatorFactory.experiment(request) } returns evaluator

            // when
            val actual = sut.experiment(request)

            // then
            expectThat(actual) isSameInstanceAs response
            verify(exactly = 0) { evaluator.record(any(), any()) }
        }
    }

    @Nested
    inner class RemoteConfigTest {

        @Test
        fun `factory 에서 찾은 evaluator 로 평가하고 기록한다`() {
            // given
            val request = RemoteConfigs.localRequest()
            val response = mockk<RemoteConfigEvaluateResponse>()
            val evaluator = mockk<RemoteConfigEvaluator<RemoteConfigEvaluateRequest>> {
                every { evaluate(request, any()) } returns response
                justRun { record(request, response) }
            }
            every { evaluatorFactory.remoteConfig(request) } returns evaluator

            // when
            val actual = sut.remoteConfig(request)

            // then
            expectThat(actual) isSameInstanceAs response
            verify(exactly = 1) { evaluator.record(request, response) }
        }
    }

    @Nested
    inner class InAppMessageEligibilityTest {

        @Test
        fun `factory 에서 찾은 eligibility evaluator 로 평가하고 기록한다`() {
            // given
            val request = InAppMessages.eligibilityLocalRequest()
            val response = mockk<InAppMessageEligibilityEvaluateResponse>()
            val evaluator = mockk<InAppMessageEligibilityEvaluator<InAppMessageEligibilityEvaluateRequest>> {
                every { evaluate(request, any()) } returns response
                justRun { record(request, response) }
            }
            every { evaluatorFactory.inAppMessage(request) } returns evaluator

            // when
            val actual = sut.inAppMessage(request)

            // then
            expectThat(actual) isSameInstanceAs response
            verify(exactly = 1) { evaluator.record(request, response) }
        }
    }

    @Nested
    inner class InAppMessageLayoutTest {

        @Test
        fun `factory 에서 찾은 layout evaluator 로 평가하고 기록한다`() {
            // given
            val request = InAppMessages.layoutLocalRequest()
            val response = mockk<InAppMessageLayoutEvaluateResponse>()
            val evaluator = mockk<InAppMessageLayoutEvaluator<InAppMessageLayoutEvaluateRequest>> {
                every { evaluate(request, any()) } returns response
                justRun { record(request, response) }
            }
            every { evaluatorFactory.inAppMessage(request) } returns evaluator

            // when
            val actual = sut.inAppMessage(request)

            // then
            expectThat(actual) isSameInstanceAs response
            verify(exactly = 1) { evaluator.record(request, response) }
        }
    }
}
