package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Version
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class OperatorMatcherTest {
    @Nested
    inner class InMatcherTest {

        private val sut = InMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("abc")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("abc", "def")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("abc1")))
        }

        @Test
        fun `number`() {
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 320, matchValues = listOf(320)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 320.0, matchValues = listOf(320)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 320.1, matchValues = listOf(BigDecimal("320.10"))))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = BigDecimal("320.00"), matchValues = listOf(320)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = BigDecimal("320.01"), matchValues = listOf(320)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 321, matchValues = listOf(320)))
        }

        @Test
        fun `boolean`() {
            assertTrue(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(true)))
            assertTrue(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(true)))
        }

        @Test
        fun `version`() {
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("2.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValues = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValues = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValues = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class ContainsMatcherTest {

        private val sut = ContainsMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("abc")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("b")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("c")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("ab")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("ac")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("ab")))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValues = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 11, matchValues = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(true)))
        }

        @Test
        fun `version`() {
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("2.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValues = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValues = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValues = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class StartsWithMatcherTest {

        private val sut = StartsWithMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("abc")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("b")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("ab")))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValues = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 11, matchValues = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(true)))
        }

        @Test
        fun `version`() {
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("2.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValues = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValues = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValues = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class EndWithMatcherTest {

        private val sut = EndsWithMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("abc")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("c")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValues = listOf("bc")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("ab")))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValues = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 11, matchValues = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(true)))
        }

        @Test
        fun `version`() {
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("2.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValues = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValues = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValues = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class GreaterThanMatcherTest {

        private val sut = GreaterThanMatcher

        @Test
        fun `string`() {
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "41", matchValues = listOf("42")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "42", matchValues = listOf("42")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "43", matchValues = listOf("42")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230114", matchValues = listOf("20230115")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230115", matchValues = listOf("20230115")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230116", matchValues = listOf("20230115")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-14", matchValues = listOf("2023-01-15")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-15", matchValues = listOf("2023-01-15")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-16", matchValues = listOf("2023-01-15")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("A")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "A", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "aa", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("aa")))
        }

        @Test
        fun `number`() {
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 1.001, matchValues = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 2, matchValues = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValues = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 0.999, matchValues = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(true)))
        }

        @Test
        fun `version`() {
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("2.0.0"))))
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("2.0.0"), matchValues = listOf(v("1.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValues = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValues = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValues = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class GreaterThanOrEqualToMatcherTest {

        private val sut = GreaterThanOrEqualToMatcher

        @Test
        fun `string`() {
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "41", matchValues = listOf("42")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "42", matchValues = listOf("42")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "43", matchValues = listOf("42")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230114", matchValues = listOf("20230115")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230115", matchValues = listOf("20230115")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230116", matchValues = listOf("20230115")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-14", matchValues = listOf("2023-01-15")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-15", matchValues = listOf("2023-01-15")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-16", matchValues = listOf("2023-01-15")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("A")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "A", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "aa", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("aa")))
        }

        @Test
        fun `number`() {
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 1.001, matchValues = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 2, matchValues = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValues = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 0.999, matchValues = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(true)))
        }

        @Test
        fun `version`() {
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("2.0.0"))))
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("2.0.0"), matchValues = listOf(v("1.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValues = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValues = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValues = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class LessThanMatcherTest {

        private val sut = LessThanMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "41", matchValues = listOf("42")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "42", matchValues = listOf("42")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "43", matchValues = listOf("42")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230114", matchValues = listOf("20230115")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230115", matchValues = listOf("20230115")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230116", matchValues = listOf("20230115")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-14", matchValues = listOf("2023-01-15")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-15", matchValues = listOf("2023-01-15")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-16", matchValues = listOf("2023-01-15")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("A")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "A", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "aa", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("aa")))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1.001, matchValues = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 2, matchValues = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValues = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 0.999, matchValues = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(true)))
        }

        @Test
        fun `version`() {
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("1.0.0"))))
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("2.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("2.0.0"), matchValues = listOf(v("1.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValues = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValues = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValues = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class LessThanOrEqualToMatcherTest {

        private val sut = LessThanOrEqualToMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "41", matchValues = listOf("42")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "42", matchValues = listOf("42")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "43", matchValues = listOf("42")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230114", matchValues = listOf("20230115")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230115", matchValues = listOf("20230115")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230116", matchValues = listOf("20230115")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-14", matchValues = listOf("2023-01-15")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-15", matchValues = listOf("2023-01-15")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-16", matchValues = listOf("2023-01-15")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("A")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "A", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "aa", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValues = listOf("aa")))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1.001, matchValues = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 2, matchValues = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValues = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 0.999, matchValues = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValues = listOf(true)))
        }

        @Test
        fun `version`() {
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("1.0.0"))))
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = listOf(v("2.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("2.0.0"), matchValues = listOf(v("1.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValues = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValues = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValues = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValues = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class ExistMatcherTest() {
        @Test
        fun `if null fail`() {
            assertFalse(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = null, matchValues = emptyList()))
            assertFalse(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = null, matchValues = emptyList()))
            assertFalse(ExistsMatcher.matches(valueMatcher = BooleanMatcher, userValue = null, matchValues = emptyList()))
            assertFalse(ExistsMatcher.matches(valueMatcher = VersionMatcher, userValue = null, matchValues = emptyList()))
        }

        @Test
        fun `if not null success`() {
            assertTrue(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = 320, matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = true, matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = v("1.0.0"), matchValues = emptyList()))

            assertTrue(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = "abc", matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = 320, matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = true, matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = v("1.0.0"), matchValues = emptyList()))

            assertTrue(ExistsMatcher.matches(valueMatcher = BooleanMatcher, userValue = "abc", matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = BooleanMatcher, userValue = 320, matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = BooleanMatcher, userValue = true, matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = BooleanMatcher, userValue = v("1.0.0"), matchValues = emptyList()))

            assertTrue(ExistsMatcher.matches(valueMatcher = VersionMatcher, userValue = "abc", matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = VersionMatcher, userValue = 320, matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = VersionMatcher, userValue = true, matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValues = emptyList()))
        }
    }

    private fun v(version: String): Version = Version.parseOrNull(version)!!
}
