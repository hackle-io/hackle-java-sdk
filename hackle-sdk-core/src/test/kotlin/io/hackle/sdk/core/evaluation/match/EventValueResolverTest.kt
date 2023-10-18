package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.model.Target
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class EventValueResolverTest {

    @InjectMockKs
    private lateinit var sut: EventValueResolver


    @Test
    fun `이벤트 프로퍼티 타입을 받아 프로퍼티를 가져온다`() {
        val track = mockk<UserEvent.Track>()
        val key = mockk<Target.Key>()
        every { track.event.properties[any()] } returns "targetPropertyValue"
        every { key.name } returns "targetPropertyName"
        every { key.type } returns Target.Key.Type.EVENT_PROPERTY

        assertDoesNotThrow { sut.resolveOrNull(track, key) }
    }

    @Test
    fun `이벤트 프로퍼티가 아닌 경우 Exception 을 발생 시킨다`() {
        val track = mockk<UserEvent.Track>()

        every { track.event.properties[any()] } returns "targetPropertyValue"

        val key = mockk<Target.Key>()
        every { key.name } returns "targetPropertyName"

        assertThrows<IllegalArgumentException> {
            every { key.type } returns Target.Key.Type.AB_TEST
            sut.resolveOrNull(track, key)
        }
        assertThrows<IllegalArgumentException> {
            every { key.type } returns Target.Key.Type.USER_ID
            sut.resolveOrNull(track, key)
        }
        assertThrows<IllegalArgumentException> {
            every { key.type } returns Target.Key.Type.HACKLE_PROPERTY
            sut.resolveOrNull(track, key)
        }
        assertThrows<IllegalArgumentException> {
            every { key.type } returns Target.Key.Type.FEATURE_FLAG
            sut.resolveOrNull(track, key)
        }
        assertThrows<IllegalArgumentException> {
            every { key.type } returns Target.Key.Type.SEGMENT
            sut.resolveOrNull(track, key)
        }
        assertThrows<IllegalArgumentException> {
            every { key.type } returns Target.Key.Type.USER_PROPERTY
            sut.resolveOrNull(track, key)
        }
        assertThrows<IllegalArgumentException> {
            every { key.type } returns Target.Key.Type.COHORT
            sut.resolveOrNull(track, key)
        }
        assertDoesNotThrow {
            every { key.type } returns Target.Key.Type.EVENT_PROPERTY
            sut.resolveOrNull(track, key)
        }
    }
}
