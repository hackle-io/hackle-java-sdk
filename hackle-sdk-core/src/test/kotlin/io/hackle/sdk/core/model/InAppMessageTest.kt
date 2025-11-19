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

    @Nested
    inner class TimetableTest {
        @Test
        fun `all timetable should return true for any timestamp`() {
            val timetable = InAppMessage.Timetable.All
            expectThat(timetable.within(0)).isTrue()
            expectThat(timetable.within(Long.MAX_VALUE)).isTrue()
            expectThat(timetable.within(1762171200000L)).isTrue()
        }

        @Test
        fun `custom timetable should return true when timestamp matches any slot`() {
            val mondaySlot = InAppMessage.TimetableSlot(
                dayOfWeek = DayOfWeek.MONDAY,
                startMillisInclusive = 9 * 60 * 60 * 1000L, // 09:00
                endMillisExclusive = 18 * 60 * 60 * 1000L    // 18:00
            )
            val tuesdaySlot = InAppMessage.TimetableSlot(
                dayOfWeek = DayOfWeek.TUESDAY,
                startMillisInclusive = 10 * 60 * 60 * 1000L, // 10:00
                endMillisExclusive = 17 * 60 * 60 * 1000L    // 17:00
            )
            val timetable = InAppMessage.Timetable.Custom(listOf(mondaySlot, tuesdaySlot))

            // 2025-11-03T12:00:00.000Z (Monday 12:00 - matches mondaySlot)
            expectThat(timetable.within(1762171200000L)).isTrue()
            // 2025-11-04T14:00:00.000Z (Tuesday 14:00 - matches tuesdaySlot)
            expectThat(timetable.within(1762264800000L)).isTrue()
        }

        @Test
        fun `custom timetable should return false when timestamp matches no slots`() {
            val mondaySlot = InAppMessage.TimetableSlot(
                dayOfWeek = DayOfWeek.MONDAY,
                startMillisInclusive = 9 * 60 * 60 * 1000L, // 09:00
                endMillisExclusive = 18 * 60 * 60 * 1000L    // 18:00
            )
            val timetable = InAppMessage.Timetable.Custom(listOf(mondaySlot))

            // 2025-11-03T08:00:00.000Z (Monday 08:00 - before slot start)
            expectThat(timetable.within(1762156800000L)).isFalse()
            // 2025-11-03T19:00:00.000Z (Monday 19:00 - after slot end)
            expectThat(timetable.within(1762196400000L)).isFalse()
            // 2025-11-04T12:00:00.000Z (Tuesday 12:00 - different day)
            expectThat(timetable.within(1762257600000L)).isFalse()
        }

        @Test
        fun `custom timetable with empty slots should return false for any timestamp`() {
            val timetable = InAppMessage.Timetable.Custom(emptyList())
            expectThat(timetable.within(0)).isFalse()
            expectThat(timetable.within(1762171200000L)).isFalse()
        }
    }

    @Nested
    inner class TimetableSlotTest {
        @Test
        fun `slot should match when timestamp is within day and time range`() {
            val slot = InAppMessage.TimetableSlot(
                dayOfWeek = DayOfWeek.MONDAY,
                startMillisInclusive = 9 * 60 * 60 * 1000L, // 09:00
                endMillisExclusive = 18 * 60 * 60 * 1000L    // 18:00
            )

            // 2025-11-03T09:00:00.000Z (Monday 09:00 - start time inclusive)
            expectThat(slot.within(1762160400000L)).isTrue()
            // 2025-11-03T12:00:00.000Z (Monday 12:00 - within range)
            expectThat(slot.within(1762171200000L)).isTrue()
            // 2025-11-03T17:59:59.999Z (Monday 17:59:59.999 - just before end)
            expectThat(slot.within(1762192799999L)).isTrue()
        }

        @Test
        fun `slot should not match when timestamp is outside time range`() {
            val slot = InAppMessage.TimetableSlot(
                dayOfWeek = DayOfWeek.MONDAY,
                startMillisInclusive = 9 * 60 * 60 * 1000L, // 09:00
                endMillisExclusive = 18 * 60 * 60 * 1000L    // 18:00
            )

            // 2025-11-03T08:59:59.999Z (Monday 08:59:59.999 - before start)
            expectThat(slot.within(1762160399999L)).isFalse()
            // 2025-11-03T18:00:00.000Z (Monday 18:00 - end time exclusive)
            expectThat(slot.within(1762192800000L)).isFalse()
            // 2025-11-03T19:00:00.000Z (Monday 19:00 - after end)
            expectThat(slot.within(1762196400000L)).isFalse()
        }

        @Test
        fun `slot should not match when day of week is different`() {
            val slot = InAppMessage.TimetableSlot(
                dayOfWeek = DayOfWeek.MONDAY,
                startMillisInclusive = 9 * 60 * 60 * 1000L, // 09:00
                endMillisExclusive = 18 * 60 * 60 * 1000L    // 18:00
            )

            // 2025-11-04T12:00:00.000Z (Tuesday 12:00 - different day)
            expectThat(slot.within(1762257600000L)).isFalse()
            // 2025-11-05T12:00:00.000Z (Wednesday 12:00 - different day)
            expectThat(slot.within(1762344000000L)).isFalse()
        }

        @Test
        fun `slot should handle midnight boundary correctly`() {
            val slot = InAppMessage.TimetableSlot(
                dayOfWeek = DayOfWeek.MONDAY,
                startMillisInclusive = 0L, // 00:00
                endMillisExclusive = 24 * 60 * 60 * 1000L    // 24:00 (next day 00:00)
            )

            // 2025-11-03T00:00:00.000Z (Monday 00:00 - start of day)
            expectThat(slot.within(1762128000000L)).isTrue()
            // 2025-11-03T23:59:59.999Z (Monday 23:59:59.999 - end of day)
            expectThat(slot.within(1762214399999L)).isTrue()
        }

        @Test
        fun `multiple slots on different days should work correctly`() {
            val mondayMorning = InAppMessage.TimetableSlot(
                dayOfWeek = DayOfWeek.MONDAY,
                startMillisInclusive = 9 * 60 * 60 * 1000L,
                endMillisExclusive = 12 * 60 * 60 * 1000L
            )
            val mondayAfternoon = InAppMessage.TimetableSlot(
                dayOfWeek = DayOfWeek.MONDAY,
                startMillisInclusive = 14 * 60 * 60 * 1000L,
                endMillisExclusive = 18 * 60 * 60 * 1000L
            )

            // 2025-11-03T10:00:00.000Z (Monday 10:00 - morning slot)
            expectThat(mondayMorning.within(1762164000000L)).isTrue()
            expectThat(mondayAfternoon.within(1762164000000L)).isFalse()

            // 2025-11-03T15:00:00.000Z (Monday 15:00 - afternoon slot)
            expectThat(mondayMorning.within(1762182000000L)).isFalse()
            expectThat(mondayAfternoon.within(1762182000000L)).isTrue()

            // 2025-11-03T13:00:00.000Z (Monday 13:00 - between slots)
            expectThat(mondayMorning.within(1762174800000L)).isFalse()
            expectThat(mondayAfternoon.within(1762174800000L)).isFalse()
        }
    }
}
