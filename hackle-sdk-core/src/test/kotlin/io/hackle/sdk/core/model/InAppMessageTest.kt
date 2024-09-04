package io.hackle.sdk.core.model

import io.hackle.sdk.common.HackleInAppMessageActionType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
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
    }
}
