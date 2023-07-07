package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageRequest
import io.hackle.sdk.core.model.InAppMessage
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.*
import java.lang.IllegalArgumentException

@ExtendWith(MockKExtension::class)
internal class DefaultInAppMessageResolverTest {

    @InjectMockKs
    private lateinit var sut: DefaultInAppMessageResolver


    @Test
    fun `MessageContext 언어에 맞는 메시지를 리턴한다`() {
        val request = mockk<InAppMessageRequest>()
        val inAppMessage = mockk<InAppMessage>()
        val korMessage = mockk<InAppMessage.MessageContext.Message>()
        every { request.inAppMessage } returns inAppMessage
        every { inAppMessage.messageContext.defaultLang } returns "ko"
        every { korMessage.lang } returns "ko"
        every { inAppMessage.messageContext.messages } returns listOf(
            korMessage
        )
        val actual = sut.resolve(request, mockk())

        expectThat(actual){
            get { lang } isEqualTo "ko"
        }
    }

    @Test
    fun `MessageContext 언어에 맞는 메시지가 없으면 예외를 던진다`(){
        val request = mockk<InAppMessageRequest>()
        val inAppMessage = mockk<InAppMessage>()
        val engMessage = mockk<InAppMessage.MessageContext.Message>()
        every { request.inAppMessage } returns inAppMessage
        every { inAppMessage.key } returns 123L
        every { inAppMessage.messageContext.defaultLang } returns "ko"
        every { engMessage.lang } returns "en"
        every { inAppMessage.messageContext.messages } returns listOf(
            engMessage
        )

       val exception = assertThrows<IllegalArgumentException> {
            sut.resolve(request, mockk())
        }

        expectThat(exception.message){
            isNotNull()
                .startsWith("InAppMessage must be decided ${123L}")
        }
    }

}
