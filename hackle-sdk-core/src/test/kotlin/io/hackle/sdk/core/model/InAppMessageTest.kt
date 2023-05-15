package io.hackle.sdk.core.model

import io.hackle.sdk.core.model.InAppMessage.MessageContext.Orientation.HORIZONTAL
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
                                type = InAppMessage.MessageContext.Action.Type.CLOSE,
                                value = null
                            )
                        ),
                        InAppMessage.MessageContext.Message.Button(
                            text = "buttonTwo",
                            style = InAppMessage.MessageContext.Message.Button.Style("#FFFFFF", "#FFFFFF", "#FFFFFF"),
                            action = InAppMessage.MessageContext.Action(
                                behavior = InAppMessage.MessageContext.Action.Behavior.CLICK,
                                type = InAppMessage.MessageContext.Action.Type.CLOSE,
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
                            type = InAppMessage.MessageContext.Action.Type.CLOSE,
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
            displayTimeRange = InAppMessage.DisplayTimeRange.from(InAppMessage.TimeUnitType.IMMEDIATE, null, null),
            targetContext = targetContext,
            eventTriggerRules = listOf(eventTriggerRule),
            messageContext = messageContext
        )

        expectThat(actual) {
            get { id } isEqualTo 1L
            get { status } isEqualTo InAppMessage.Status.DRAFT
            get { key } isEqualTo 1L
            get { displayTimeRange.timeUnit } isEqualTo InAppMessage.TimeUnitType.IMMEDIATE
            get { displayTimeRange.startEpochTimeMillis } isEqualTo -1L
            get { displayTimeRange.endEpochTimeMillis } isEqualTo -1L
            get { targetContext.targets } isEqualTo emptyList()
            get { targetContext.overrides } isEqualTo emptyList()
            get { messageContext }.and {
                get { defaultLang } isEqualTo "ko"
                get { platformTypes } hasSize 2
                get { platformTypes.contains(ANDROID) } isEqualTo true
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
                            get { title } isEqualTo InAppMessage.MessageContext.Message.Text.TextAttribute(
                                text = "title",
                                style = InAppMessage.MessageContext.Message.Text.Style("#FFFFFF")
                            )
                            get { body } isEqualTo InAppMessage.MessageContext.Message.Text.TextAttribute(
                                text = "body",
                                style = InAppMessage.MessageContext.Message.Text.Style("#FFFFFF")
                            )
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
                                    get { type } isEqualTo InAppMessage.MessageContext.Action.Type.CLOSE
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
                                    get { type } isEqualTo InAppMessage.MessageContext.Action.Type.CLOSE
                                    get { value }.isNull()
                                }
                            }
                        }
                        get { background.color } isEqualTo "#FFFFFF"
                        get { closeButton }.and {
                            isNotNull()
                            get { this!!.style.color } isEqualTo "#FFFFFF"
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
            type = InAppMessage.MessageContext.Action.Type.WEB_LINK,
            value = "test://activity"
        )
        expectThat(action.behavior) isEqualTo InAppMessage.MessageContext.Action.Behavior.CLICK
        expectThat(InAppMessage.MessageContext.Action.Type.from("WEB_LINK")) isEqualTo InAppMessage.MessageContext.Action.Type.WEB_LINK
        expectThat(action.value) { isNotNull() }
    }

    @Test
    fun `Orientation`() {
        expectThat(InAppMessage.MessageContext.Orientation.from("VERTICAL")) isEqualTo VERTICAL
        expectThat(InAppMessage.MessageContext.Orientation.from("vertical")) isEqualTo VERTICAL
        expectThat(InAppMessage.MessageContext.Orientation.from("HORIZONTAL")) isEqualTo HORIZONTAL
        expectThat(InAppMessage.MessageContext.Orientation.from("horizontal")) isEqualTo HORIZONTAL
        expectThat(InAppMessage.MessageContext.Orientation.from("Unsupported")).isNull()
    }


    @Test
    fun `status`() {
        expectThat(InAppMessage.Status.from("INITIALIZED")) isEqualTo InAppMessage.Status.INITIALIZED
        expectThat(InAppMessage.Status.from("DRAFT")) isEqualTo InAppMessage.Status.DRAFT
        expectThat(InAppMessage.Status.from("ACTIVE")) isEqualTo InAppMessage.Status.ACTIVE
        expectThat(InAppMessage.Status.from("PAUSE")) isEqualTo InAppMessage.Status.PAUSE
        expectThat(InAppMessage.Status.from("FINISH")) isEqualTo InAppMessage.Status.FINISH
        expectThat(InAppMessage.Status.from("ARCHIVED")) isEqualTo InAppMessage.Status.ARCHIVED
        expectThat(InAppMessage.Status.from("NOT_SUPPORT_STATUS")).isNull()
    }

    @Test
    fun `timeUnit type`() {
        expectThat(InAppMessage.TimeUnitType.from("IMMEDIATE")) isEqualTo InAppMessage.TimeUnitType.IMMEDIATE
        expectThat(InAppMessage.TimeUnitType.from("CUSTOM")) isEqualTo InAppMessage.TimeUnitType.CUSTOM

        expectThat(InAppMessage.TimeUnitType.from("NOT_SUPPORT_TYPE")).isNull()
    }

    @Test
    fun `displayTimeRange 가 IMMEDIATE 인 경우 withInTimeRange 는 항상 true 를 반환한다 `() {
        val inAppMessage = mockk<InAppMessage>()
        val displayTimeRange = InAppMessage.DisplayTimeRange(
            timeUnit = InAppMessage.TimeUnitType.IMMEDIATE
        )
        every { inAppMessage.displayTimeRange } returns displayTimeRange


        val actual = inAppMessage.withInDisplayTimeRange(anyLong())

        expectThat(actual) isEqualTo true
    }


    @Test
    fun `displayTimeRange 가 CUSTOME 인 경우 currentTime 이 범위에 있어야 withInTimeRange 가 true 를 리턴한다 `() {
        val inAppMessage = mockk<InAppMessage>()
        val displayTimeRange = InAppMessage.DisplayTimeRange(
            timeUnit = InAppMessage.TimeUnitType.CUSTOM,
            startEpochTimeMillis = 0L,
            endEpochTimeMillis = 120L
        )

        every { inAppMessage.displayTimeRange } returns displayTimeRange


        val actual = inAppMessage.withInDisplayTimeRange(20L)

        expectThat(actual) isEqualTo true
    }

    @Test
    fun `displayTimeRange 가 CUSTOME 인 경우 currentTime 이 범위에 있지 않으면 withInTimeRange 가 false 를 리턴한다 `() {
        val inAppMessage = mockk<InAppMessage>()
        val displayTimeRange = InAppMessage.DisplayTimeRange(
            timeUnit = InAppMessage.TimeUnitType.CUSTOM,
            startEpochTimeMillis = 0L,
            endEpochTimeMillis = 120L
        )

        every { inAppMessage.displayTimeRange } returns displayTimeRange


        val actual = inAppMessage.withInDisplayTimeRange(240L)

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

        val displayTimeRange = InAppMessage.DisplayTimeRange.from(
            InAppMessage.TimeUnitType.CUSTOM,
            mockInAppMessageDto.startEpochTimeMills,
            mockInAppMessageDto.endEpochTimeMills
        )

        expectThat(displayTimeRange) {
            get { timeUnit } isEqualTo InAppMessage.TimeUnitType.CUSTOM
            get { startEpochTimeMillis } isEqualTo 1000L
            get { endEpochTimeMillis } isEqualTo 2000L
        }
    }

    @Test
    fun `timeUnit 이 IMMEDEATE 인 경우 epochTime 은 모두 NONE 이다`() {
        val mockInAppMessageDto = MockInAppMessageDto(
            key = 123L,
            timeUnit = "IMMEDIATE",
            startEpochTimeMills = null,
            endEpochTimeMills = null
        )

        val displayTimeRange = InAppMessage.DisplayTimeRange.from(
            InAppMessage.TimeUnitType.from(mockInAppMessageDto.timeUnit)!!,
            mockInAppMessageDto.startEpochTimeMills,
            mockInAppMessageDto.endEpochTimeMills
        )

        expectThat(displayTimeRange) {
            get { timeUnit } isEqualTo InAppMessage.TimeUnitType.IMMEDIATE
            get { startEpochTimeMillis } isEqualTo InAppMessage.DisplayTimeRange.NONE
            get { endEpochTimeMillis } isEqualTo InAppMessage.DisplayTimeRange.NONE
        }
    }

    @Test
    fun `timeUnit 이 CUSTOM 인데 start, end epoch time 이 null 인 경우 NONE 로 바꿔서 넣어준다 `() {
        val mockInAppMessageDto = MockInAppMessageDto(
            key = 123L,
            timeUnit = "CUSTOM",
            startEpochTimeMills = null,
            endEpochTimeMills = 12345L
        )

        val displayTimeRange = InAppMessage.DisplayTimeRange.from(
            InAppMessage.TimeUnitType.from(mockInAppMessageDto.timeUnit)!!,
            mockInAppMessageDto.startEpochTimeMills,
            mockInAppMessageDto.endEpochTimeMills
        )

        expectThat(displayTimeRange) {
            get { timeUnit } isEqualTo InAppMessage.TimeUnitType.CUSTOM
            get { startEpochTimeMillis } isEqualTo InAppMessage.DisplayTimeRange.NONE
            get { endEpochTimeMillis } isEqualTo 12345L
        }
    }

    @Test
    fun `timeUnit 이 CUSTOM 인 경우 start 또는 end epoch 타임 하나라도 NONE 이면 범위에 없는것으로 판단한다`() {

        val inAppMessage = mockk<InAppMessage>()

        every { inAppMessage.displayTimeRange.timeUnit } returns InAppMessage.TimeUnitType.CUSTOM
        every { inAppMessage.displayTimeRange.startEpochTimeMillis } returns InAppMessage.DisplayTimeRange.NONE
        every { inAppMessage.displayTimeRange.endEpochTimeMillis } returns 123123123L


        val actual = inAppMessage.withInDisplayTimeRange(149124L)

        expectThat(actual) isEqualTo false

    }

    @Test
    fun `timeUnit 이 CUSTOM 인 경우 start 또는 end epoch 타임 하나라도 NONE 이면 범위에 없는것으로 판단한다 -- 2 `() {

        val inAppMessage = mockk<InAppMessage>()

        every { inAppMessage.displayTimeRange.timeUnit } returns InAppMessage.TimeUnitType.CUSTOM
        every { inAppMessage.displayTimeRange.startEpochTimeMillis } returns 123123123L
        every { inAppMessage.displayTimeRange.endEpochTimeMillis } returns InAppMessage.DisplayTimeRange.NONE


        val actual = inAppMessage.withInDisplayTimeRange(149124L)

        expectThat(actual) isEqualTo false

    }

    @Test
    fun `timeUnit 이 CUSTOM이고 start 와 end epoch 타임이 모두 NONE 이 아닌 경우에 currentMillis 가 범위에 있으면 withinDisplayTimeRange 는 true 를 리턴한다`() {
        val inAppMessage = mockk<InAppMessage>()

        every { inAppMessage.displayTimeRange.timeUnit } returns InAppMessage.TimeUnitType.CUSTOM
        every { inAppMessage.displayTimeRange.startEpochTimeMillis } returns 1L
        every { inAppMessage.displayTimeRange.endEpochTimeMillis } returns 100L

        val actual = inAppMessage.withInDisplayTimeRange(50L)

        expectThat(actual) isEqualTo true
    }

    @Test
    fun `timeUnit 이 CUSTOM이고 start 와 end epoch 타임이 모두 NONE 이 아닌 경우에 currentMillis 가 범위에 들지 않으면 withinDisplayTimeRange 는 false 를 리턴한다`() {
        val inAppMessage = mockk<InAppMessage>()

        every { inAppMessage.displayTimeRange.timeUnit } returns InAppMessage.TimeUnitType.CUSTOM
        every { inAppMessage.displayTimeRange.startEpochTimeMillis } returns 1L
        every { inAppMessage.displayTimeRange.endEpochTimeMillis } returns 100L

        val actual = inAppMessage.withInDisplayTimeRange(101L)

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
