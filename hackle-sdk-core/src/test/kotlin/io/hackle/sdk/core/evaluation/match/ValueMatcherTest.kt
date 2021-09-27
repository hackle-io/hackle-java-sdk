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
        fun `userValue, matchValue가 String타입이면 OperatorMatcher의 일치 결과로 평가한다`() {
            // given
            val userValue = "value1"
            val matchValue = "value1"
            val operatorMatcher = mockk<OperatorMatcher> {
                every { matches(userValue, matchValue) } returns true
            }

            val sut = StringMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertTrue(actual)
        }

        @Test
        fun `userValue가 String타입이 아니면 false`() {
            // given
            val userValue = 1
            val matchValue = "1"
            val operatorMatcher = mockk<OperatorMatcher>()

            val sut = StringMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertFalse(actual)
            verify { operatorMatcher wasNot Called }
        }

        @Test
        fun `userValue가 String타입이지만 matchValue가 String타입이 아니면 false`() {
            // given
            val userValue = "1"
            val matchValue = 1
            val operatorMatcher = mockk<OperatorMatcher>()

            val sut = StringMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertFalse(actual)
            verify { operatorMatcher wasNot Called }
        }
    }

    @Nested
    inner class NumberMatcherTest {

        @Test
        fun `userValue, matchValue가 Number타입이면 OperatorMatcher의 일치 결과로 평가한다`() {
            // given
            val userValue = 42
            val matchValue = 42
            val operatorMatcher = mockk<OperatorMatcher> {
                every { matches(userValue, matchValue) } returns true
            }

            val sut = NumberMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertTrue(actual)
        }

        @Test
        fun `userValue가 Number타입이 아니면 false`() {
            // given
            val userValue = "1"
            val matchValue = 1
            val operatorMatcher = mockk<OperatorMatcher>()

            val sut = NumberMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertFalse(actual)
            verify { operatorMatcher wasNot Called }
        }

        @Test
        fun `userValue가 Number타입이지만 matchValue가 Number타입이 아니면 false`() {
            // given
            val userValue = 1
            val matchValue = "1"
            val operatorMatcher = mockk<OperatorMatcher>()

            val sut = NumberMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertFalse(actual)
            verify { operatorMatcher wasNot Called }
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
            val userValue = "false"
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
            val matchValue = "false"
            val operatorMatcher = mockk<OperatorMatcher>()

            val sut = BooleanMatcher

            // when
            val actual = sut.matches(operatorMatcher, userValue, matchValue)

            // then
            assertFalse(actual)
            verify { operatorMatcher wasNot Called }
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