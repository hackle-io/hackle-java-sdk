package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentLocalEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local.InAppMessageEligibilityLocalEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.local.InAppMessageLayoutLocalEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.mode.local.RemoteConfigLocalEvaluator
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.InAppMessages
import io.hackle.sdk.core.support.RemoteConfigs
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

internal class EvaluatorFactoryTest {

    private val sut = EvaluatorFactory()

    @Test
    fun `request 를 지원하는 evaluator 를 리턴한다`() {
        // given
        val unsupported = mockk<ExperimentLocalEvaluator> {
            every { supports(any()) } returns false
        }
        val supported = mockk<ExperimentLocalEvaluator> {
            every { supports(any()) } returns true
        }
        sut.add(unsupported)
        sut.add(supported)

        // when
        val actual = sut.get(Experiments.localRequest())

        // then
        expectThat(actual) isSameInstanceAs supported
    }

    @Test
    fun `지원하는 evaluator 가 없으면 예외 발생`() {
        val exception = assertThrows<IllegalArgumentException> {
            sut.get(Experiments.localRequest())
        }

        expectThat(exception.message).isNotNull().startsWith("Not Found Evaluator")
    }

    @Test
    fun `experiment - ExperimentEvaluator 를 리턴한다`() {
        // given
        val evaluator = mockk<ExperimentLocalEvaluator> {
            every { supports(any()) } returns true
        }
        sut.add(evaluator)

        // when
        val actual = sut.experiment(Experiments.localRequest())

        // then
        expectThat(actual) isSameInstanceAs evaluator
    }

    @Test
    fun `remoteConfig - RemoteConfigEvaluator 를 리턴한다`() {
        // given
        val evaluator = mockk<RemoteConfigLocalEvaluator> {
            every { supports(any()) } returns true
        }
        sut.add(evaluator)

        // when
        val actual = sut.remoteConfig(RemoteConfigs.localRequest())

        // then
        expectThat(actual) isSameInstanceAs evaluator
    }

    @Test
    fun `inAppMessage - InAppMessageEligibilityEvaluator 를 리턴한다`() {
        // given
        val evaluator = mockk<InAppMessageEligibilityLocalEvaluator> {
            every { supports(any()) } returns true
        }
        sut.add(evaluator)

        // when
        val actual = sut.inAppMessage(InAppMessages.eligibilityLocalRequest())

        // then
        expectThat(actual) isSameInstanceAs evaluator
    }

    @Test
    fun `inAppMessage - InAppMessageLayoutEvaluator 를 리턴한다`() {
        // given
        val evaluator = mockk<InAppMessageLayoutLocalEvaluator> {
            every { supports(any()) } returns true
        }
        sut.add(evaluator)

        // when
        val actual = sut.inAppMessage(InAppMessages.layoutLocalRequest())

        // then
        expectThat(actual) isSameInstanceAs evaluator
    }
}
