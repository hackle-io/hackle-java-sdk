package io.hackle.sdk.core.model

import io.hackle.sdk.core.model.InAppMessage.MessageContext.Action.Type
import io.hackle.sdk.core.model.InAppMessage.MessageContext.Orientation.VERTICAL
import io.hackle.sdk.core.model.InAppMessage.MessageContext.PlatformType.ANDROID
import io.hackle.sdk.core.model.InAppMessage.MessageContext.PlatformType.WEB
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class InAppMessageTest {

    @Test
    fun `create`() {

        val targetContext = InAppMessage.TargetContext(
            emptyList(),
            emptyList()
        )

        val messageContext = InAppMessage.MessageContext(
            defaultLang = "ko",
            platformTypes = listOf(ANDROID, WEB),
            exposure = InAppMessage.MessageContext.Exposure(
                InAppMessage.MessageContext.Exposure.Type.DEFAULT,
                123L
            ),
            messages = listOf(
                InAppMessage.MessageContext.Message(
                    lang = "ko",
                    layout = InAppMessage.MessageContext.Message.Layout(
                        InAppMessage.MessageContext.Message.Layout.DisplayType.MODAL,
                        InAppMessage.MessageContext.Message.Layout.LayoutType.IMAGE_ONLY
                    ),
                    images = listOf(
                        InAppMessage.MessageContext.Message.Image(
                            orientation = VERTICAL,
                            imagePath = "http://localhost",
                            action = null
                        )
                    ),
                    text = InAppMessage.MessageContext.Message.Text(
                        InAppMessage.MessageContext.Message.Text.TextAttribute(
                            text = "title",
                            style = InAppMessage.MessageContext.Message.Text.Style("#FFFFFF")
                        ),
                        InAppMessage.MessageContext.Message.Text.TextAttribute(
                            text = "body",
                            style = InAppMessage.MessageContext.Message.Text.Style("#FFFFFF")
                        )
                    ),
                    buttons = listOf(
                        InAppMessage.MessageContext.Message.Button(
                            text = "buttonOne",
                            style = InAppMessage.MessageContext.Message.Button.Style("#FFFFFF", "#FFFFFF", "#FFFFFF"),
                            action = InAppMessage.MessageContext.Action(
                                behavior = InAppMessage.MessageContext.Action.Behavior.CLICK,
                                type = Type.CLOSE,
                                value = null
                            )
                        ),
                        InAppMessage.MessageContext.Message.Button(
                            text = "buttonTwo",
                            style = InAppMessage.MessageContext.Message.Button.Style("#FFFFFF", "#FFFFFF", "#FFFFFF"),
                            action = InAppMessage.MessageContext.Action(
                                behavior = InAppMessage.MessageContext.Action.Behavior.CLICK,
                                type = Type.CLOSE,
                                value = null
                            )
                        )
                    ),
                    background = InAppMessage.MessageContext.Message.Background("#FFFFFF"),
                    closeButton = InAppMessage.MessageContext.Message.CloseButton(
                        style = InAppMessage.MessageContext.Message.CloseButton.Style(
                            "#FFFFFF"
                        ),
                        action = InAppMessage.MessageContext.Action(
                            behavior = InAppMessage.MessageContext.Action.Behavior.CLICK,
                            type = Type.CLOSE,
                            value = null
                        )
                    )
                )
            ),
            orientations = listOf(VERTICAL)
        )

        val eventTriggerRule = InAppMessage.EventTriggerRule(
            eventKey = "test",
            targets = emptyList()
        )

        val actual = inAppMessage(
            displayTimeRange = InAppMessage.Range.Immediate,
            targetContext = targetContext,
            eventTriggerRules = listOf(eventTriggerRule),
            messageContext = messageContext
        )

        expectThat(actual) {
            get { id } isEqualTo 1L
            get { status } isEqualTo InAppMessage.Status.DRAFT
            get { key } isEqualTo 1L
            get { displayTimeRange } isEqualTo InAppMessage.Range.Immediate
            get { targetContext.targets } isEqualTo emptyList()
            get { targetContext.overrides } isEqualTo emptyList()
            get { messageContext }.and {
                get { defaultLang } isEqualTo "ko"
                get { platformTypes } hasSize 2
                get { platformTypes.contains(ANDROID) } isEqualTo true
                get { orientations.contains(VERTICAL) } isEqualTo true
                get { exposure.type } isEqualTo InAppMessage.MessageContext.Exposure.Type.DEFAULT
                get { exposure.key } isEqualTo 123L
                get { messages }.and {
                    hasSize(1)
                    get { first() }.and {
                        get { lang } isEqualTo "ko"
                        get { layout.displayType } isEqualTo InAppMessage.MessageContext.Message.Layout.DisplayType.MODAL
                        get { layout.layoutType } isEqualTo InAppMessage.MessageContext.Message.Layout.LayoutType.IMAGE_ONLY
                        get { images }.and {
                            hasSize(1)
                            get { first() }.and {
                                get { orientation } isEqualTo VERTICAL
                                get { imagePath } isEqualTo "http://localhost"
                                get { action }.isNull()
                            }
                        }
                        get { text }.isNotNull().and {
                            get { title }.and {
                                get { text } isEqualTo "title"
                                get { style.textColor } isEqualTo "#FFFFFF"
                            }
                            get { body }.and {
                                get { text } isEqualTo "body"
                                get { style.textColor } isEqualTo "#FFFFFF"
                            }

                        }
                        get { buttons }.and {
                            hasSize(2)
                            get { first() }.and {
                                get { text } isEqualTo "buttonOne"
                                get { style }.and {
                                    get { textColor } isEqualTo "#FFFFFF"
                                    get { bgColor } isEqualTo "#FFFFFF"
                                    get { borderColor } isEqualTo "#FFFFFF"
                                }
                                get { action }.and {
                                    get { behavior } isEqualTo InAppMessage.MessageContext.Action.Behavior.CLICK
                                    get { type } isEqualTo Type.CLOSE
                                    get { value }.isNull()
                                }
                            }
                            get { last() }.and {
                                get { text } isEqualTo "buttonTwo"
                                get { style }.and {
                                    get { textColor } isEqualTo "#FFFFFF"
                                    get { bgColor } isEqualTo "#FFFFFF"
                                    get { borderColor } isEqualTo "#FFFFFF"
                                }
                                get { action }.and {
                                    get { behavior } isEqualTo InAppMessage.MessageContext.Action.Behavior.CLICK
                                    get { type } isEqualTo Type.CLOSE
                                    get { value }.isNull()
                                }
                            }
                        }
                        get { background.color } isEqualTo "#FFFFFF"
                        get { closeButton }.and {
                            isNotNull()
                            get { this!!.style.color } isEqualTo "#FFFFFF"
                            get { this!!.action }.and {
                                get { behavior } isEqualTo InAppMessage.MessageContext.Action.Behavior.CLICK
                                get { type } isEqualTo Type.CLOSE
                                get { value } isEqualTo null
                            }
                        }
                    }
                }
            }
            get { eventTriggerRules }.and {
                hasSize(1)
                contains(eventTriggerRule)
            }
            get { eventTriggerRules.first() }.and {
                get { eventKey } isEqualTo "test"
                get { targets } isEqualTo emptyList()
            }
        }
    }


    @Test
    fun `action`() {
        val action = InAppMessage.MessageContext.Action(
            behavior = InAppMessage.MessageContext.Action.Behavior.CLICK,
            type = Type.WEB_LINK,
            value = "test://activity"
        )
        expectThat(action.behavior) isEqualTo InAppMessage.MessageContext.Action.Behavior.CLICK
        expectThat(action.type) isEqualTo Type.WEB_LINK
        expectThat(action.value) { isNotNull() }
    }

    @Test
    fun `displayTimeRange 가 IMMEDIATE 인 경우 withInTimeRange 는 항상 true 를 반환한다 `() {
        val inAppMessage = mockk<InAppMessage>()
        val displayTimeRange = InAppMessage.Range.Immediate
        every { inAppMessage.displayTimeRange } returns displayTimeRange


        val actual = inAppMessage.displayTimeRange.within(anyLong())

        expectThat(actual) isEqualTo true
    }


    @Test
    fun `displayTimeRange 가 CUSTOME 인 경우 currentTime 이 범위에 있어야 withInTimeRange 가 true 를 리턴한다 `() {
        val inAppMessage = mockk<InAppMessage>()

        val displayTimeRange = InAppMessage.Range.Custom(
            startEpochTimeMillis = 0L,
            endEpochTimeMillis = 120L
        )
        every { inAppMessage.displayTimeRange } returns displayTimeRange


        val actual = inAppMessage.displayTimeRange.within(20L)

        expectThat(actual) isEqualTo true
    }

    @Test
    fun `displayTimeRange 가 CUSTOME 인 경우 currentTime 이 범위에 있지 않으면 withInTimeRange 가 false 를 리턴한다 `() {
        val inAppMessage = mockk<InAppMessage>()
        val displayTimeRange = InAppMessage.Range.Custom(
            startEpochTimeMillis = 0L,
            endEpochTimeMillis = 120L
        )

        every { inAppMessage.displayTimeRange } returns displayTimeRange


        val actual = inAppMessage.displayTimeRange.within(240L)

        expectThat(actual) isEqualTo false
    }

    @Test
    fun `인앱 메시지 dto 정보로 부터 CUSTOM DisplayTimeRange 를 만든다`() {
        val mockInAppMessageDto = MockInAppMessageDto(
            key = 123L,
            timeUnit = "CUSTOM",
            startEpochTimeMills = 1000L,
            endEpochTimeMills = 2000L
        )
        val displayTimeRange = InAppMessage.Range.Custom(
            mockInAppMessageDto.startEpochTimeMills!!,
            mockInAppMessageDto.endEpochTimeMills!!
        )

        expectThat(displayTimeRange) {
            isA<InAppMessage.Range.Custom>()
            get { startEpochTimeMillis } isEqualTo 1000L
            get { endEpochTimeMillis } isEqualTo 2000L
        }
    }


    @Test
    fun `timeUnit 이 CUSTOM이고 start 와 end epoch 타임이 모두 NONE 이 아닌 경우에 currentMillis 가 범위에 있으면 withinDisplayTimeRange 는 true 를 리턴한다`() {
        val inAppMessage = mockk<InAppMessage>()

        every { inAppMessage.displayTimeRange } returns InAppMessage.Range.Custom(1L, 100L)

        val actual = inAppMessage.displayTimeRange.within(50L)

        expectThat(actual) isEqualTo true
    }

    @Test
    fun `timeUnit 이 CUSTOM이고 start 와 end epoch 타임이 모두 NONE 이 아닌 경우에 currentMillis 가 범위에 들지 않으면 withinDisplayTimeRange 는 false 를 리턴한다`() {
        val inAppMessage = mockk<InAppMessage>()
        every { inAppMessage.displayTimeRange } returns InAppMessage.Range.Custom(1L, 100L)

        val actual = inAppMessage.displayTimeRange.within(101L)

        expectThat(actual) isEqualTo false
    }

    @Test
    fun `timeUnit 이 CUSTOM이고 start 와 end epoch 타임이 모두 NONE 이 아닌 경우에 currentMillis 가 범위에 들지 않으면 withinDisplayTimeRange 는 false 를 리턴한다 -- 2`() {
        val inAppMessage = mockk<InAppMessage>()
        every { inAppMessage.displayTimeRange } returns InAppMessage.Range.Custom(1L, 100L)

        val actual = inAppMessage.displayTimeRange.within(0L)

        expectThat(actual) isEqualTo false
    }

    data class MockInAppMessageDto(
        val key: Long,
        val timeUnit: String,
        val startEpochTimeMills: Long?,
        val endEpochTimeMills: Long?
    )

    private fun anyLong(): Long {
        return Random(0).nextLong()
    }

}
