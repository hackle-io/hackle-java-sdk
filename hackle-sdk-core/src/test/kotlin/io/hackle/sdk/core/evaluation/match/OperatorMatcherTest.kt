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
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValue = listOf("abc")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValue = listOf("abc", "def")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValue = listOf("abc1")))
        }

        @Test
        fun `number`() {
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 320, matchValue = listOf(320)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 320.0, matchValue = listOf(320)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 320.1, matchValue = listOf(BigDecimal("320.10"))))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = BigDecimal("320.00"), matchValue = listOf(320)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = BigDecimal("320.01"), matchValue = listOf(320)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 321, matchValue = listOf(320)))
        }

        @Test
        fun `boolean`() {
            assertTrue(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(true)))
            assertTrue(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(true)))
        }

        @Test
        fun `version`() {
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("2.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValue = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValue = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValue = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class ContainsMatcherTest {

        private val sut = ContainsMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("abc")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("b")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("c")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("ab")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("ac")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("ab")))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValue = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 11, matchValue = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(true)))
        }

        @Test
        fun `version`() {
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("2.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValue = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValue = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValue = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class StartsWithMatcherTest {

        private val sut = StartsWithMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("abc")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("b")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("ab")))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValue = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 11, matchValue = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(true)))
        }

        @Test
        fun `version`() {
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("2.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValue = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValue = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValue = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class EndWithMatcherTest {

        private val sut = EndsWithMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("abc")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("c")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "abc", matchValue = listOf("bc")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("ab")))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValue = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 11, matchValue = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(true)))
        }

        @Test
        fun `version`() {
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("2.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValue = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValue = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValue = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class GreaterThanMatcherTest {

        private val sut = GreaterThanMatcher

        @Test
        fun `string`() {
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "41", matchValue = listOf("42")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "42", matchValue = listOf("42")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "43", matchValue = listOf("42")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230114", matchValue = listOf("20230115")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230115", matchValue = listOf("20230115")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230116", matchValue = listOf("20230115")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-14", matchValue = listOf("2023-01-15")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-15", matchValue = listOf("2023-01-15")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-16", matchValue = listOf("2023-01-15")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("A")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "A", matchValue = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "aa", matchValue = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("aa")))
        }

        @Test
        fun `number`() {
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 1.001, matchValue = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 2, matchValue = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValue = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 0.999, matchValue = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(true)))
        }

        @Test
        fun `version`() {
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("2.0.0"))))
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("2.0.0"), matchValue = listOf(v("1.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValue = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValue = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValue = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class GreaterThanOrEqualToMatcherTest {

        private val sut = GreaterThanOrEqualToMatcher

        @Test
        fun `string`() {
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "41", matchValue = listOf("42")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "42", matchValue = listOf("42")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "43", matchValue = listOf("42")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230114", matchValue = listOf("20230115")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230115", matchValue = listOf("20230115")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230116", matchValue = listOf("20230115")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-14", matchValue = listOf("2023-01-15")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-15", matchValue = listOf("2023-01-15")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-16", matchValue = listOf("2023-01-15")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("A")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "A", matchValue = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "aa", matchValue = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("aa")))
        }

        @Test
        fun `number`() {
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 1.001, matchValue = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 2, matchValue = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValue = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 0.999, matchValue = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(true)))
        }

        @Test
        fun `version`() {
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("1.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("2.0.0"))))
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("2.0.0"), matchValue = listOf(v("1.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValue = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValue = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValue = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class LessThanMatcherTest {

        private val sut = LessThanMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "41", matchValue = listOf("42")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "42", matchValue = listOf("42")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "43", matchValue = listOf("42")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230114", matchValue = listOf("20230115")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230115", matchValue = listOf("20230115")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230116", matchValue = listOf("20230115")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-14", matchValue = listOf("2023-01-15")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-15", matchValue = listOf("2023-01-15")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-16", matchValue = listOf("2023-01-15")))

            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("A")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "A", matchValue = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "aa", matchValue = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("aa")))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1.001, matchValue = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 2, matchValue = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValue = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 0.999, matchValue = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(true)))
        }

        @Test
        fun `version`() {
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("1.0.0"))))
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("2.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("2.0.0"), matchValue = listOf(v("1.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValue = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValue = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValue = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class LessThanOrEqualToMatcherTest {

        private val sut = LessThanOrEqualToMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "41", matchValue = listOf("42")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "42", matchValue = listOf("42")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "43", matchValue = listOf("42")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230114", matchValue = listOf("20230115")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "20230115", matchValue = listOf("20230115")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "20230116", matchValue = listOf("20230115")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-14", matchValue = listOf("2023-01-15")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-15", matchValue = listOf("2023-01-15")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "2023-01-16", matchValue = listOf("2023-01-15")))

            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("A")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "A", matchValue = listOf("a")))
            assertFalse(sut.matches(valueMatcher  = StringMatcher, userValue = "aa", matchValue = listOf("a")))
            assertTrue(sut.matches(valueMatcher  = StringMatcher, userValue = "a", matchValue = listOf("aa")))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 1.001, matchValue = listOf(1)))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = 2, matchValue = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 1, matchValue = listOf(1)))
            assertTrue(sut.matches(valueMatcher = NumberMatcher, userValue = 0.999, matchValue = listOf(1)))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = listOf(false)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = false, matchValue = listOf(true)))
        }

        @Test
        fun `version`() {
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("1.0.0"))))
            assertTrue(sut.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = listOf(v("2.0.0"))))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = v("2.0.0"), matchValue = listOf(v("1.0.0"))))
        }

        @Test
        fun `if null fail`() {
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = null, matchValue = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = NumberMatcher, userValue = null, matchValue = listOf(320)))
            assertFalse(sut.matches(valueMatcher = BooleanMatcher, userValue = null, matchValue = listOf(true)))
            assertFalse(sut.matches(valueMatcher = VersionMatcher, userValue = null, matchValue = listOf(v("1.0.0"))))
        }
    }

    @Nested
    inner class ExistMatcherTest() {
        @Test
        fun `if null fail`() {
            assertFalse(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = null, matchValue = emptyList()))
            assertFalse(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = null, matchValue = emptyList()))
            assertFalse(ExistsMatcher.matches(valueMatcher = BooleanMatcher, userValue = null, matchValue = emptyList()))
            assertFalse(ExistsMatcher.matches(valueMatcher = VersionMatcher, userValue = null, matchValue = emptyList()))
        }

        @Test
        fun `if not null success`() {
            assertTrue(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = "abc", matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = 320, matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = true, matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = v("1.0.0"), matchValue = emptyList()))

            assertTrue(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = "abc", matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = 320, matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = true, matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = v("1.0.0"), matchValue = emptyList()))

            assertTrue(ExistsMatcher.matches(valueMatcher = BooleanMatcher, userValue = "abc", matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = BooleanMatcher, userValue = 320, matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = BooleanMatcher, userValue = true, matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = BooleanMatcher, userValue = v("1.0.0"), matchValue = emptyList()))

            assertTrue(ExistsMatcher.matches(valueMatcher = VersionMatcher, userValue = "abc", matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = VersionMatcher, userValue = 320, matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = VersionMatcher, userValue = true, matchValue = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = VersionMatcher, userValue = v("1.0.0"), matchValue = emptyList()))
        }
    }

    private fun v(version: String): Version = Version.parseOrNull(version)!!
}
