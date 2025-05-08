package io.hackle.sdk.core.model

import io.hackle.sdk.common.HackleInAppMessageActionType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.*

class InAppMessageTest {
    
    @Nested
    inner class ActionTest {

        @Test
        fun `public type`() {
            fun expectedType(actionType: InAppMessage.ActionType): HackleInAppMessageActionType {
                return when (actionType) {
                    InAppMessage.ActionType.WEB_LINK -> HackleInAppMessageActionType.LINK
                    InAppMessage.ActionType.CLOSE -> HackleInAppMessageActionType.CLOSE
                    InAppMessage.ActionType.HIDDEN -> HackleInAppMessageActionType.CLOSE
                    InAppMessage.ActionType.LINK_AND_CLOSE -> HackleInAppMessageActionType.LINK
                }
            }

            for (actionType in InAppMessage.ActionType.values()) {
                val expectedType = expectedType(actionType)
                val action = InAppMessage.Action(InAppMessage.Behavior.CLICK, actionType, "value")
                expectThat(action.type).isEqualTo(expectedType)
            }
        }

        @Test
        fun `action`() {
            expectThat(InAppMessage.Action(InAppMessage.Behavior.CLICK, InAppMessage.ActionType.CLOSE, "value")) {
                get { close }.isNotNull().and {
                    get { hideDurationMillis }.isNull()
                }
                get { link }.isNull()
            }
            expectThat(InAppMessage.Action(InAppMessage.Behavior.CLICK, InAppMessage.ActionType.HIDDEN, "value")) {
                get { close }.isNotNull().and {
                    get { hideDurationMillis }.isEqualTo(1000 * 60 * 60 * 24)
                }
                get { link }.isNull()
            }
            expectThat(InAppMessage.Action(InAppMessage.Behavior.CLICK, InAppMessage.ActionType.WEB_LINK, "value")) {
                get { close }.isNull()
                get { link }.isNotNull().and {
                    get { url } isEqualTo "value"
                    get { shouldCloseAfterLink }.isFalse()
                }
            }
            expectThat(
                InAppMessage.Action(
                    InAppMessage.Behavior.CLICK,
                    InAppMessage.ActionType.LINK_AND_CLOSE,
                    "value"
                )
            ) {
                get { close }.isNull()
                get { link }.isNotNull().and {
                    get { url } isEqualTo "value"
                    get { shouldCloseAfterLink }.isTrue()
                }
            }
        }

        @Test
        fun `should throw IllegalArgumentException when value is null for link actions`() {
            expectThrows<IllegalArgumentException> {
                InAppMessage.Action(InAppMessage.Behavior.CLICK, InAppMessage.ActionType.WEB_LINK, null).link
            }
            expectThrows<IllegalArgumentException> {
                InAppMessage.Action(InAppMessage.Behavior.CLICK, InAppMessage.ActionType.LINK_AND_CLOSE, null).link
            }
        }
    }

    @Nested
    inner class PeriodTest {
        @Test
        fun `always period should return true for any timestamp`() {
            val period = InAppMessage.Period.Always
            expectThat(period.within(0)).isTrue()
            expectThat(period.within(Long.MAX_VALUE)).isTrue()
            expectThat(period.within(Long.MIN_VALUE)).isTrue()
        }

        @Test
        fun `custom period should return true when timestamp is within range`() {
            val period = InAppMessage.Period.Custom(100L, 200L)
            expectThat(period.within(100L)).isTrue()
            expectThat(period.within(150L)).isTrue()
            expectThat(period.within(199L)).isTrue()
        }

        @Test
        fun `custom period should return false when timestamp is outside range`() {
            val period = InAppMessage.Period.Custom(100L, 200L)
            expectThat(period.within(99L)).isFalse()
            expectThat(period.within(200L)).isFalse()
            expectThat(period.within(201L)).isFalse()
        }
    }
}
