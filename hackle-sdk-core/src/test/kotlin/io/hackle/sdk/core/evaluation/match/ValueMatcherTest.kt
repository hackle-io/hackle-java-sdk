package io.hackle.sdk.core.evaluation.match

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ValueMatcherTest {
    class tmp {}
    @Nested
    inner class StringMatcherTest {
        @Test
        fun `string type match`() {
            assertTrue(StringMatcher.inMatch("42", "42"))
            assertTrue(StringMatcher.inMatch("42.42", "42.42"))
            assertTrue(StringMatcher.containsMatch("42", "4"))
            assertTrue(StringMatcher.startsWithMatch("42", "4"))
            assertTrue(StringMatcher.endsWithMatch("42", "2"))
            assertTrue(StringMatcher.greaterThanMatch("42", "41"))
            assertTrue(StringMatcher.greaterThanOrEqualToMatch("42", "42"))
            assertTrue(StringMatcher.greaterThanOrEqualToMatch("43", "42"))
            assertTrue(StringMatcher.lessThanMatch("41", "42"))
            assertTrue(StringMatcher.lessThanOrEqualToMatch("42", "42"))
            assertTrue(StringMatcher.lessThanOrEqualToMatch("41", "42"))
        }

        @Test
        fun `target이 자료형이 아니면 항상 false`() {
            assertFalse(StringMatcher.inMatch("42", tmp()))
            assertFalse(StringMatcher.containsMatch("42", tmp()))
            assertFalse(StringMatcher.startsWithMatch("42", tmp()))
            assertFalse(StringMatcher.endsWithMatch("42", tmp()))
            assertFalse(StringMatcher.greaterThanMatch("42", tmp()))
            assertFalse(StringMatcher.greaterThanOrEqualToMatch("42", tmp()))
            assertFalse(StringMatcher.lessThanMatch("42", tmp()))
            assertFalse(StringMatcher.lessThanOrEqualToMatch("42", tmp()))
        }

        @Test
        fun `userValue가 자료형이 아니면 항상 false`() {
            assertFalse(StringMatcher.inMatch(tmp(), "42"))
            assertFalse(StringMatcher.containsMatch(tmp(), "42"))
            assertFalse(StringMatcher.startsWithMatch(tmp(), "42"))
            assertFalse(StringMatcher.endsWithMatch(tmp(), "42"))
            assertFalse(StringMatcher.greaterThanMatch(tmp(), "42"))
            assertFalse(StringMatcher.greaterThanOrEqualToMatch(tmp(), "42"))
            assertFalse(StringMatcher.lessThanMatch(tmp(), "42"))
            assertFalse(StringMatcher.lessThanOrEqualToMatch(tmp(), "42"))
        }

        @Test
        fun `number 타입이면 캐스팅 후 match`() {
            assertTrue(StringMatcher.inMatch("42", 42))
            assertTrue(StringMatcher.inMatch(42, "42"))
            assertTrue(StringMatcher.inMatch(42, 42))

            assertTrue(StringMatcher.inMatch(42.42, "42.42"))
            assertTrue(StringMatcher.inMatch("42.42", 42.42))
            assertTrue(StringMatcher.inMatch(42.42, 42.42))

            assertTrue(StringMatcher.inMatch("42.0", 42.0))
            assertTrue(StringMatcher.inMatch(42.0, "42.0"))
            assertTrue(StringMatcher.inMatch(42.0, 42.0))
        }

        @Test
        fun `boolean 타입이면 캐스팅 후 match`() {
            assertTrue(StringMatcher.inMatch("true", true))
            assertTrue(StringMatcher.inMatch(true, "true"))
            assertTrue(StringMatcher.inMatch(true, true))
            assertTrue(StringMatcher.inMatch("false", false))
            assertTrue(StringMatcher.inMatch(false, "false"))
            assertTrue(StringMatcher.inMatch(false, false))

            assertFalse(StringMatcher.inMatch(true, "TRUE"))
            assertFalse(StringMatcher.inMatch(false, "FALSE"))
        }

        @Test
        fun `지원하지 않는 타입`() {
            assertFalse(StringMatcher.inMatch(true, "1"))
            assertFalse(StringMatcher.inMatch("1", true))
        }


    }

    @Nested
    inner class NumberMatcherTest {

        @Test
        fun `number type match`() {
            assertTrue(NumberMatcher.inMatch(42, 42))
            assertTrue(NumberMatcher.inMatch(42.42, 42.42))
            assertTrue(NumberMatcher.inMatch(42, 42.0))
            assertTrue(NumberMatcher.inMatch(42.0, 42))
            assertTrue(NumberMatcher.inMatch(42L, 42))
            assertTrue(NumberMatcher.inMatch(42, 42L))
            assertTrue(NumberMatcher.inMatch(0, 0.0))
            assertTrue(NumberMatcher.inMatch(0.0, 0))

            assertTrue(NumberMatcher.greaterThanMatch(42, 41))
            assertTrue(NumberMatcher.greaterThanMatch(42.42, 42.41))
            assertTrue(NumberMatcher.greaterThanMatch(42, 41.0))
            assertTrue(NumberMatcher.greaterThanMatch(42.0, 41))
            assertTrue(NumberMatcher.greaterThanMatch(42L, 41))
            assertTrue(NumberMatcher.greaterThanMatch(42, 41L))
            assertTrue(NumberMatcher.greaterThanMatch(0, -1))
            assertTrue(NumberMatcher.greaterThanMatch(0.0, -1))

            assertTrue(NumberMatcher.greaterThanOrEqualToMatch(42, 42))
            assertTrue(NumberMatcher.greaterThanOrEqualToMatch(42.42, 42.42))
            assertTrue(NumberMatcher.greaterThanOrEqualToMatch(42, 42.0))
            assertTrue(NumberMatcher.greaterThanOrEqualToMatch(42.0, 42))
            assertTrue(NumberMatcher.greaterThanOrEqualToMatch(42L, 42))
            assertTrue(NumberMatcher.greaterThanOrEqualToMatch(42, 42L))
            assertTrue(NumberMatcher.greaterThanOrEqualToMatch(0, 0.0))
            assertTrue(NumberMatcher.greaterThanOrEqualToMatch(0.0, 0))

            assertTrue(NumberMatcher.lessThanMatch(41, 42))
            assertTrue(NumberMatcher.lessThanMatch(42.41, 42.42))
            assertTrue(NumberMatcher.lessThanMatch(41, 42.0))
            assertTrue(NumberMatcher.lessThanMatch(41.0, 42))
            assertTrue(NumberMatcher.lessThanMatch(41L, 42))
            assertTrue(NumberMatcher.lessThanMatch(41, 42L))
            assertTrue(NumberMatcher.lessThanMatch(-1, 0))
            assertTrue(NumberMatcher.lessThanMatch(-1.0, 0))

            assertTrue(NumberMatcher.lessThanOrEqualToMatch(42, 42))
            assertTrue(NumberMatcher.lessThanOrEqualToMatch(42.42, 42.42))
            assertTrue(NumberMatcher.lessThanOrEqualToMatch(42, 42.0))
            assertTrue(NumberMatcher.lessThanOrEqualToMatch(42.0, 42))
            assertTrue(NumberMatcher.lessThanOrEqualToMatch(42L, 42))
            assertTrue(NumberMatcher.lessThanOrEqualToMatch(42, 42L))
            assertTrue(NumberMatcher.lessThanOrEqualToMatch(0, 0.0))
            assertTrue(NumberMatcher.lessThanOrEqualToMatch(0.0, 0))
        }

        @Test
        fun `target이 숫자형이 아니면 항상 false`() {
            assertFalse(NumberMatcher.inMatch(42, "string"))
            assertFalse(NumberMatcher.containsMatch(42, "string"))
            assertFalse(NumberMatcher.startsWithMatch(42, false))
            assertFalse(NumberMatcher.endsWithMatch(42, "string"))
            assertFalse(NumberMatcher.greaterThanMatch(42, false))
            assertFalse(NumberMatcher.greaterThanOrEqualToMatch(42, true))
            assertFalse(NumberMatcher.lessThanMatch(42, "1.1.1"))
            assertFalse(NumberMatcher.lessThanOrEqualToMatch(42, "string"))
        }

        @Test
        fun `userValue가 숫자형 아니면 항상 false`() {
            assertFalse(NumberMatcher.inMatch("string", 42))
            assertFalse(NumberMatcher.containsMatch(false, 42))
            assertFalse(NumberMatcher.startsWithMatch("string", 42))
            assertFalse(NumberMatcher.endsWithMatch(false, 42))
            assertFalse(NumberMatcher.greaterThanMatch(false, 42))
            assertFalse(NumberMatcher.greaterThanOrEqualToMatch(true, 42))
            assertFalse(NumberMatcher.lessThanMatch("1.1.1", 42))
            assertFalse(NumberMatcher.lessThanOrEqualToMatch("string", 42))
        }

        @Test
        fun `string 타입이면 캐스팅 후 match`() {
            assertTrue(NumberMatcher.inMatch("42", "42"))
            assertTrue(NumberMatcher.inMatch("42", 42))
            assertTrue(NumberMatcher.inMatch(42, "42"))

            assertTrue(NumberMatcher.inMatch("42.42", "42.42"))
            assertTrue(NumberMatcher.inMatch("42.42", 42.42))
            assertTrue(NumberMatcher.inMatch(42.42, "42.42"))

            assertTrue(NumberMatcher.inMatch("42.0", "42.0"))
            assertTrue(NumberMatcher.inMatch("42.0", 42.0))
            assertTrue(NumberMatcher.inMatch(42.0, "42.0"))
        }

        @Test
        fun `지원하지 않는 타입`() {
            assertFalse(NumberMatcher.inMatch("42a", 42))
            assertFalse(NumberMatcher.inMatch(0, "false"))
            assertFalse(NumberMatcher.inMatch(0, false))
            assertFalse(NumberMatcher.inMatch(true, true))
        }

        @Test
        fun `지원하지 않는 연산자`() {
            assertFalse(NumberMatcher.containsMatch(42, 42))
            assertFalse(NumberMatcher.startsWithMatch(42, 42))
            assertFalse(NumberMatcher.endsWithMatch(42, 42))
        }
    }

    @Nested
    inner class BooleanMatcherTest {
        @Test
        fun `boolean type matcher`() {
            assertTrue(BooleanMatcher.inMatch(true, true))
            assertTrue(BooleanMatcher.inMatch(false, false))
            assertFalse(BooleanMatcher.inMatch(true, false))
            assertFalse(BooleanMatcher.inMatch(false, true))

            assertTrue(BooleanMatcher.inMatch("true", true))
            assertTrue(BooleanMatcher.inMatch("false", false))
            assertTrue(BooleanMatcher.inMatch(true, "true"))
            assertTrue(BooleanMatcher.inMatch(false, "false"))
            assertFalse(BooleanMatcher.inMatch("true", false))
            assertFalse(BooleanMatcher.inMatch("false", true))

            assertFalse(BooleanMatcher.inMatch("TRUE", true))
            assertFalse(BooleanMatcher.inMatch("FALSE", false))
            assertFalse(BooleanMatcher.inMatch(true, "TRUE"))
            assertFalse(BooleanMatcher.inMatch(false, "FALSE"))

            assertFalse(BooleanMatcher.inMatch("true", 1))
            assertFalse(BooleanMatcher.inMatch(1, "true"))
            assertFalse(BooleanMatcher.inMatch("true", 1.0))
            assertFalse(BooleanMatcher.inMatch(1.0, "true"))
            assertFalse(BooleanMatcher.inMatch("true", "1"))
            assertFalse(BooleanMatcher.inMatch("1", "true"))

            assertFalse(BooleanMatcher.inMatch("false", 1))
            assertFalse(BooleanMatcher.inMatch(1, "false"))
            assertFalse(BooleanMatcher.inMatch("false", 1.0))
            assertFalse(BooleanMatcher.inMatch(1.0, "false"))

            assertFalse(BooleanMatcher.inMatch("true", 1L))
            assertFalse(BooleanMatcher.inMatch(1L, "true"))
            assertFalse(BooleanMatcher.inMatch("string", true))
            assertFalse(BooleanMatcher.inMatch(true, "string"))
        }

        @Test
        fun `지원하지 않는 연산자`() {
            assertFalse(BooleanMatcher.containsMatch(true, true))
            assertFalse(BooleanMatcher.startsWithMatch(true, true))
            assertFalse(BooleanMatcher.endsWithMatch(true, true))
            assertFalse(BooleanMatcher.greaterThanMatch(true, true))
            assertFalse(BooleanMatcher.greaterThanOrEqualToMatch(true, true))
            assertFalse(BooleanMatcher.lessThanMatch(true, true))
            assertFalse(BooleanMatcher.lessThanOrEqualToMatch(true, true))
        }
    }

    @Nested
    inner class VersionMatcherTest {

        @Test
        fun `version type match`() {
            assertTrue(VersionMatcher.inMatch("1.0.0", "1.0.0"))
            assertFalse(VersionMatcher.inMatch("1.0.0", "2.0.0"))
            assertTrue(VersionMatcher.greaterThanMatch("1.0.1", "1.0.0"))
            assertTrue(VersionMatcher.greaterThanOrEqualToMatch("1.0.0", "1.0.0"))
            assertTrue(VersionMatcher.lessThanOrEqualToMatch("1.0.0", "1.0.0"))
            assertTrue(VersionMatcher.lessThanMatch("0.0.9", "1.0.0"))
            assertTrue(VersionMatcher.lessThanOrEqualToMatch("1.0.0", "1.0.0"))
            assertTrue(VersionMatcher.lessThanOrEqualToMatch("1.0.0", "1.0.1"))
        }

        @Test
        fun `target이 Version타입이 아니면 false`() {
            assertFalse(VersionMatcher.inMatch(1, "1.0.0"))
            assertFalse(VersionMatcher.inMatch("1.0.0", 1))
            assertFalse(VersionMatcher.containsMatch("1.0.0", true))
            assertFalse(VersionMatcher.startsWithMatch("1.0.0", true))
            assertFalse(VersionMatcher.endsWithMatch("1.0.0", true))
            assertFalse(VersionMatcher.greaterThanMatch("1.0.0", true))
            assertFalse(VersionMatcher.greaterThanOrEqualToMatch("1.0.0", true))
            assertFalse(VersionMatcher.lessThanMatch("1.0.0", true))
            assertFalse(VersionMatcher.lessThanOrEqualToMatch("1.0.0", true))
        }

        @Test
        fun `userValue가 Version타입이 아니면 false`() {
            assertFalse(VersionMatcher.inMatch(true, "1.0.0"))
            assertFalse(VersionMatcher.containsMatch(true, "1.0.0"))
            assertFalse(VersionMatcher.startsWithMatch(true, "1.0.0"))
            assertFalse(VersionMatcher.endsWithMatch(true, "1.0.0"))
            assertFalse(VersionMatcher.greaterThanMatch(true, "1.0.0"))
            assertFalse(VersionMatcher.greaterThanOrEqualToMatch(true, "1.0.0"))
            assertFalse(VersionMatcher.lessThanMatch(true, "1.0.0"))
            assertFalse(VersionMatcher.lessThanOrEqualToMatch(true, "1.0.0"))
        }

        @Test
        fun `지원하지 않는 연산자`() {
            assertFalse(VersionMatcher.containsMatch("1.0.0", "1.0.0"))
            assertFalse(VersionMatcher.startsWithMatch("1.0.0", "1.0.0"))
            assertFalse(VersionMatcher.endsWithMatch("1.0.0", "1.0.0"))
        }
    }
}