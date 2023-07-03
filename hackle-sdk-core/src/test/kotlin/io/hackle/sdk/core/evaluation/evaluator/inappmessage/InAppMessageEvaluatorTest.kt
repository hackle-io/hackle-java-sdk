package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.storage.HackleInAppMessageStorage
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigRequest
import io.hackle.sdk.core.evaluation.target.InAppMessageResolver
import io.hackle.sdk.core.evaluation.target.InAppMessageTargetDeterminer
import io.hackle.sdk.core.evaluation.target.InAppMessageUserOverrideDeterminer
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.model.InAppMessage.MessageContext.PlatformType.*
import io.hackle.sdk.core.model.withInDisplayTimeRange
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
internal class InAppMessageEvaluatorTest {

    @MockK
    private lateinit var inAppMessageTargetDeterminer: InAppMessageTargetDeterminer

    @MockK
    private lateinit var inAppMessageUserOverrideDeterminer: InAppMessageUserOverrideDeterminer

    @MockK
    private lateinit var hackleInAppMessageStorage: HackleInAppMessageStorage


    @MockK
    private lateinit var inAppMessageResolver: InAppMessageResolver

    @InjectMockKs
    private lateinit var sut: InAppMessageEvaluator


    private lateinit var inAppMessage: InAppMessage
    private lateinit var request: InAppMessageRequest
    private lateinit var message: InAppMessage.MessageContext.Message

    @Test
    fun supports() {
        Assertions.assertTrue(sut.supports(mockk<InAppMessageRequest>()))
        Assertions.assertFalse(sut.supports(mockk<RemoteConfigRequest<Any>>()))
        Assertions.assertFalse(sut.supports(mockk<ExperimentRequest>()))
    }


    @BeforeEach
    fun setup() {
        inAppMessage = mockk()
        message = mockk()
        request = inAppMessageRequest(inAppMessage = inAppMessage, currentTimeMillis = NOW)
        mockkStatic("io.hackle.sdk.core.model.InAppMessageKt")
        every { message.lang } returns "ko"
        every { inAppMessage.key } returns 123L
        every { inAppMessage.messageContext } returns mockk()
        every { inAppMessage.messageContext.defaultLang } returns "ko"
        every { inAppMessage.messageContext.messages } returns listOf(message)

        every { inAppMessage.displayTimeRange.timeUnit } returns InAppMessage.TimeUnitType.CUSTOM
        every { inAppMessage.displayTimeRange.startEpochTimeMillis } returns NOW - 1L
        every { inAppMessage.displayTimeRange.endEpochTimeMillis } returns NOW + 1L

        every { inAppMessageResolver.resolve(any(), any()) } returns message
    }


    @Test
    fun `플랫폼타입에 ANDROID 가 없으면 보여주지 않음`() {
        every { inAppMessage.messageContext.platformTypes } returns listOf(WEB, IOS)

        val evaluation = sut.evaluate(request, Evaluators.context())

        expectThat(evaluation) {
            get { isShow } isEqualTo false
            get { reason } isEqualTo DecisionReason.UNSUPPORTED_PLATFORM
        }
    }


    @Test
    fun `오버라이드 조건이 true 이면 보여줌`() {
        every { inAppMessage.messageContext.platformTypes } returns listOf(ANDROID)
        every { inAppMessageUserOverrideDeterminer.determine(any()) } returns true

        val evaluation = sut.evaluate(request, Evaluators.context())

        expectThat(evaluation) {
            get { isShow } isEqualTo true
            get { reason } isEqualTo DecisionReason.OVERRIDDEN
        }
    }


    @Test
    fun `오버라이드 조건 false, status DRAFT 이면 안보여줌`() {
        every { inAppMessage.messageContext.platformTypes } returns listOf(ANDROID)
        every { inAppMessageUserOverrideDeterminer.determine(any()) } returns false
        every { inAppMessage.status } returns InAppMessage.Status.DRAFT

        val evaluation = sut.evaluate(request, Evaluators.context())

        expectThat(evaluation) {
            get { isShow } isEqualTo false
            get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_DRAFT
        }
    }

    @Test
    fun `오버라이드 조건 false, status PAUSE 이면 안보여줌`() {
        every { inAppMessage.messageContext.platformTypes } returns listOf(ANDROID)
        every { inAppMessageUserOverrideDeterminer.determine(any()) } returns false
        every { inAppMessage.status } returns InAppMessage.Status.PAUSE

        val evaluation = sut.evaluate(request, Evaluators.context())

        expectThat(evaluation) {
            get { isShow } isEqualTo false
            get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_PAUSED
        }
    }



    @Test
    fun `오버라이드 조건 false, status ACTIVE, display 시간이 안 맞으면 안보여줌`() {
        every { inAppMessage.messageContext.platformTypes } returns listOf(ANDROID)
        every { inAppMessageUserOverrideDeterminer.determine(any()) } returns false
        every { inAppMessage.status } returns InAppMessage.Status.ACTIVE
        every { inAppMessage.withInDisplayTimeRange(any()) } returns false

        val evaluation = sut.evaluate(request, Evaluators.context())

        expectThat(evaluation) {
            get { isShow } isEqualTo false
            get { reason } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_PERIOD
        }

    }

    @Test
    fun `오버라이드 조건 false, status ACTIVE, display 시간 맞음, Hidden 상태이면 보여주지 않음`() {
        every { inAppMessage.messageContext.platformTypes } returns listOf(ANDROID)
        every { inAppMessageUserOverrideDeterminer.determine(any()) } returns false
        every { inAppMessage.status } returns InAppMessage.Status.ACTIVE
        every { inAppMessage.withInDisplayTimeRange(any()) } returns true
        every { hackleInAppMessageStorage.exist(any(),any()) } returns false

        val evaluation = sut.evaluate(request, Evaluators.context())

        expectThat(evaluation) {
            get { isShow } isEqualTo false
            get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_HIDDEN
        }

    }

    @Test
    fun `오버라이드 조건 false, status ACTIVE, display 시간 맞음, Hidden 상태 아니고 타겟팅 조건에 맞으면 보여줌`() {
        every { inAppMessage.messageContext.platformTypes } returns listOf(ANDROID)
        every { inAppMessageUserOverrideDeterminer.determine(any()) } returns false
        every { inAppMessageTargetDeterminer.determine(any(), any()) } returns true
        every { inAppMessage.status } returns InAppMessage.Status.ACTIVE
        every { inAppMessage.withInDisplayTimeRange(any()) } returns true
        every { hackleInAppMessageStorage.exist(any(),any()) } returns false

        val evaluation = sut.evaluate(request, Evaluators.context())

        expectThat(evaluation) {
            get { isShow } isEqualTo true
            get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_TARGET
        }
    }


    @Test
    fun `오버라이드 조건 false, status ACTIVE, display 시간 맞음, Hidden 상태 아니고 타겟팅 조건에도 맞지 않으면 보여주지 않음`() {
        every { inAppMessage.messageContext.platformTypes } returns listOf(ANDROID)
        every { inAppMessageUserOverrideDeterminer.determine(any()) } returns false
        every { inAppMessageTargetDeterminer.determine(any(), any()) } returns false
        every { inAppMessage.status } returns InAppMessage.Status.ACTIVE
        every { inAppMessage.withInDisplayTimeRange(any()) } returns true
        every { hackleInAppMessageStorage.exist(any(),any()) } returns false

        val evaluation = sut.evaluate(request, Evaluators.context())

        expectThat(evaluation) {
            get { isShow } isEqualTo false
            get { reason } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
        }
    }


    companion object {
        private const val NOW = 1686709946304L
    }
}
