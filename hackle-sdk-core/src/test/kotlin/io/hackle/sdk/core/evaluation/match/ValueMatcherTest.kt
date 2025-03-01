package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Version
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ValueMatcherTest {

    @Nested
    inner class StringMatcherTest {
        @Test
        fun `string type match`() {
            assertTrue(StringMatcher.matches(InMatcher, "42", "42"))
        }

        @Test
        fun `number 타입이면 캐스팅 후 match`() {
            assertTrue(StringMatcher.matches(InMatcher, "42", 42))
            assertTrue(StringMatcher.matches(InMatcher, 42, "42"))
            assertTrue(StringMatcher.matches(InMatcher, 42, 42))

            assertTrue(StringMatcher.matches(InMatcher, 42.42, "42.42"))
            assertTrue(StringMatcher.matches(InMatcher, "42.42", 42.42))
            assertTrue(StringMatcher.matches(InMatcher, 42.42, 42.42))

            assertTrue(StringMatcher.matches(InMatcher, "42.0", 42.0))
            assertTrue(StringMatcher.matches(InMatcher, 42.0, "42.0"))
            assertTrue(StringMatcher.matches(InMatcher, 42.0, 42.0))
        }

        @Test
        fun `boolean 타입이면 캐스팅 후 match`() {
            assertTrue(StringMatcher.matches(InMatcher, "true", true))
            assertTrue(StringMatcher.matches(InMatcher, true, "true"))
            assertTrue(StringMatcher.matches(InMatcher, true, true))
            assertTrue(StringMatcher.matches(InMatcher, "false", false))
            assertTrue(StringMatcher.matches(InMatcher, false, "false"))
            assertTrue(StringMatcher.matches(InMatcher, false, false))

            assertFalse(StringMatcher.matches(InMatcher, true, "TRUE"))
            assertFalse(StringMatcher.matches(InMatcher, false, "FALSE"))
        }

        @Test
        fun `지원하지 않는 타입`() {
            assertFalse(StringMatcher.matches(InMatcher, true, "1"))
            assertFalse(StringMatcher.matches(InMatcher, "1", true))
        }
    }

    @Nested
    inner class NumberMatcherTest {

        @Test
        fun `number type match`() {
            assertTrue(NumberMatcher.matches(InMatcher, 42, 42))
            assertTrue(NumberMatcher.matches(InMatcher, 42.42, 42.42))
            assertTrue(NumberMatcher.matches(InMatcher, 42, 42.0))
            assertTrue(NumberMatcher.matches(InMatcher, 42.0, 42))
            assertTrue(NumberMatcher.matches(InMatcher, 42L, 42))
            assertTrue(NumberMatcher.matches(InMatcher, 42, 42L))
            assertTrue(NumberMatcher.matches(InMatcher, 0, 0.0))
            assertTrue(NumberMatcher.matches(InMatcher, 0.0, 0))
        }

        @Test
        fun `string 타입이면 캐스팅 후 match`() {
            assertTrue(NumberMatcher.matches(InMatcher, "42", "42"))
            assertTrue(NumberMatcher.matches(InMatcher, "42", 42))
            assertTrue(NumberMatcher.matches(InMatcher, 42, "42"))

            assertTrue(NumberMatcher.matches(InMatcher, "42.42", "42.42"))
            assertTrue(NumberMatcher.matches(InMatcher, "42.42", 42.42))
            assertTrue(NumberMatcher.matches(InMatcher, 42.42, "42.42"))

            assertTrue(NumberMatcher.matches(InMatcher, "42.0", "42.0"))
            assertTrue(NumberMatcher.matches(InMatcher, "42.0", 42.0))
            assertTrue(NumberMatcher.matches(InMatcher, 42.0, "42.0"))
        }

        @Test
        fun `지원하지 않는 타입`() {
            assertFalse(NumberMatcher.matches(InMatcher, "42a", 42))
            assertFalse(NumberMatcher.matches(InMatcher, 0, "false"))
            assertFalse(NumberMatcher.matches(InMatcher, 0, false))
            assertFalse(NumberMatcher.matches(InMatcher, true, true))
        }
    }

    @Nested
    inner class BooleanMatcherTest {

        @Test
        fun `userValue, matchValue가 Boolean타입이면 OperatorMatcher의 일치 결과로 평가한다`() {
            // given
            val userValue = false
            val matchValue = false
            val operatorMatcher = mockk<OperatorMatcher> {
                every { matches(userValue, matchValue) } returns true
            }

            val sut = BooleanMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertTrue(actual)
        }

        @Test
        fun `userValue가 Boolean타입이 아니면 false`() {
            // given
            val userValue = "string"
            val matchValue = false
            val operatorMatcher = mockk<OperatorMatcher>()

            val sut = BooleanMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertFalse(actual)
            verify { operatorMatcher wasNot Called }
        }

        @Test
        fun `userValue가 Boolean타입이지만 matchValue가 Boolean타입이 아니면 false`() {
            // given
            val userValue = false
            val matchValue = "string"
            val operatorMatcher = mockk<OperatorMatcher>()

            val sut = BooleanMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertFalse(actual)
            verify { operatorMatcher wasNot Called }
        }

        @Test
        fun `userValue 혹은 matchValue가 String타입이지만 true이거나 false이면 BoolMatcher의 일치 결과로 평가한다`() {
            assertTrue(BooleanMatcher.matches(InMatcher, "true", true))
            assertTrue(BooleanMatcher.matches(InMatcher, "false", false))

            assertTrue(BooleanMatcher.matches(InMatcher, true, "true"))
            assertTrue(BooleanMatcher.matches(InMatcher, false, "false"))

            assertFalse(BooleanMatcher.matches(InMatcher, "TRUE", true))
            assertFalse(BooleanMatcher.matches(InMatcher, "FALSE", false))
            assertFalse(BooleanMatcher.matches(InMatcher, true, "TRUE"))
            assertFalse(BooleanMatcher.matches(InMatcher, false, "FALSE"))
            assertFalse(BooleanMatcher.matches(InMatcher, "false", true))
            assertFalse(BooleanMatcher.matches(InMatcher, "true", false))
        }
    }

    @Nested
    inner class VersionMatcherTest {

        @Test
        fun `userValue, matchValue가 Version타입이면 OperatorMatcher의 일치 결과로 평가한다`() {
            // given
            val userValue = "1.0.0"
            val matchValue = "2.0.0"
            val operatorMatcher = mockk<OperatorMatcher> {
                every { matches(any<Version>(), any<Version>()) } returns true
            }

            val sut = VersionMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertTrue(actual)
        }

        @Test
        fun `userValue가 Version타입이 아니면 false`() {
            // given
            val userValue = 1
            val matchValue = "1.0.0"
            val operatorMatcher = mockk<OperatorMatcher>()

            val sut = VersionMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertFalse(actual)
            verify { operatorMatcher wasNot Called }
        }

        @Test
        fun `userValue가 Versio타입이지만 matchValue가 Versio타입이 아니면 false`() {
            // given
            val userValue = "1.0.0"
            val matchValue = 1
            val operatorMatcher = mockk<OperatorMatcher>()

            val sut = VersionMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertFalse(actual)
            verify { operatorMatcher wasNot Called }
        }
    }

}