package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.model.InAppMessages
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class InAppMessageResolverTest {

    @InjectMockKs
    private lateinit var sut: InAppMessageResolver

    @Test
    fun `resolve`() {
        // given
        val message = InAppMessages.message(lang = "ko")
        val inAppMessage = InAppMessages.create(
            messageContext = InAppMessages.messageContext(
                defaultLang = "ko",
                messages = listOf(message)
            )
        )
        val request = InAppMessages.request(inAppMessage = inAppMessage)

        // when
        val actual = sut.resolve(request, Evaluators.context())

        // then
        expectThat(actual) isSameInstanceAs message
    }

    @Test
    fun `cannot resolve`() {
        val message = InAppMessages.message(lang = "ko")
        val inAppMessage = InAppMessages.create(
            messageContext = InAppMessages.messageContext(
                defaultLang = "en",
                messages = listOf(message)
            )
        )
        val request = InAppMessages.request(inAppMessage = inAppMessage)

        assertThrows<IllegalArgumentException> {
            sut.resolve(request, Evaluators.context())
        }
    }
}