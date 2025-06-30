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
            assertTrue(
                sut.matches(
                    valueMatcher = NumberMatcher,
                    userValue = 320.1,
                    matchValues = listOf(BigDecimal("320.10"))
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = NumberMatcher,
                    userValue = BigDecimal("320.00"),
                    matchValues = listOf(320)
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = NumberMatcher,
                    userValue = BigDecimal("320.01"),
                    matchValues = listOf(320)
                )
            )
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
            assertTrue(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("2.0.0"))
                )
            )
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
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("abc")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("b")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("c")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("ab")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("ac")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("ab")))
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
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("2.0.0"))
                )
            )
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
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("abc")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("b")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("ab")))
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
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("2.0.0"))
                )
            )
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
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("abc")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("c")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "abc", matchValues = listOf("bc")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("ab")))
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
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("2.0.0"))
                )
            )
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
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "41", matchValues = listOf("42")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "42", matchValues = listOf("42")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "43", matchValues = listOf("42")))

            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230114",
                    matchValues = listOf("20230115")
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230115",
                    matchValues = listOf("20230115")
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230116",
                    matchValues = listOf("20230115")
                )
            )

            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-14",
                    matchValues = listOf("2023-01-15")
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-15",
                    matchValues = listOf("2023-01-15")
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-16",
                    matchValues = listOf("2023-01-15")
                )
            )

            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("A")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "A", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "aa", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("aa")))
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
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("2.0.0"))
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("2.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
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
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "41", matchValues = listOf("42")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "42", matchValues = listOf("42")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "43", matchValues = listOf("42")))

            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230114",
                    matchValues = listOf("20230115")
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230115",
                    matchValues = listOf("20230115")
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230116",
                    matchValues = listOf("20230115")
                )
            )

            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-14",
                    matchValues = listOf("2023-01-15")
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-15",
                    matchValues = listOf("2023-01-15")
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-16",
                    matchValues = listOf("2023-01-15")
                )
            )

            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("A")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "A", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "aa", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("aa")))
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
            assertTrue(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("2.0.0"))
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("2.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
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
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "41", matchValues = listOf("42")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "42", matchValues = listOf("42")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "43", matchValues = listOf("42")))

            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230114",
                    matchValues = listOf("20230115")
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230115",
                    matchValues = listOf("20230115")
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230116",
                    matchValues = listOf("20230115")
                )
            )

            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-14",
                    matchValues = listOf("2023-01-15")
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-15",
                    matchValues = listOf("2023-01-15")
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-16",
                    matchValues = listOf("2023-01-15")
                )
            )

            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("A")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "A", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "aa", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("aa")))
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
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("2.0.0"))
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("2.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
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
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "41", matchValues = listOf("42")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "42", matchValues = listOf("42")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "43", matchValues = listOf("42")))

            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230114",
                    matchValues = listOf("20230115")
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230115",
                    matchValues = listOf("20230115")
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "20230116",
                    matchValues = listOf("20230115")
                )
            )

            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-14",
                    matchValues = listOf("2023-01-15")
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-15",
                    matchValues = listOf("2023-01-15")
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = StringMatcher,
                    userValue = "2023-01-16",
                    matchValues = listOf("2023-01-15")
                )
            )

            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("A")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "A", matchValues = listOf("a")))
            assertFalse(sut.matches(valueMatcher = StringMatcher, userValue = "aa", matchValues = listOf("a")))
            assertTrue(sut.matches(valueMatcher = StringMatcher, userValue = "a", matchValues = listOf("aa")))
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
            assertTrue(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
            assertTrue(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = listOf(v("2.0.0"))
                )
            )
            assertFalse(
                sut.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("2.0.0"),
                    matchValues = listOf(v("1.0.0"))
                )
            )
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
            assertFalse(
                ExistsMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = null,
                    matchValues = emptyList()
                )
            )
            assertFalse(
                ExistsMatcher.matches(
                    valueMatcher = NumberMatcher,
                    userValue = null,
                    matchValues = emptyList()
                )
            )
            assertFalse(
                ExistsMatcher.matches(
                    valueMatcher = BooleanMatcher,
                    userValue = null,
                    matchValues = emptyList()
                )
            )
            assertFalse(
                ExistsMatcher.matches(
                    valueMatcher = VersionMatcher,
                    userValue = null,
                    matchValues = emptyList()
                )
            )
        }

        @Test
        fun `if not null success`() {
            assertTrue(
                ExistsMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abc",
                    matchValues = emptyList()
                )
            )
            assertTrue(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = 320, matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = StringMatcher, userValue = true, matchValues = emptyList()))
            assertTrue(
                ExistsMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = v("1.0.0"),
                    matchValues = emptyList()
                )
            )

            assertTrue(
                ExistsMatcher.matches(
                    valueMatcher = NumberMatcher,
                    userValue = "abc",
                    matchValues = emptyList()
                )
            )
            assertTrue(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = 320, matchValues = emptyList()))
            assertTrue(ExistsMatcher.matches(valueMatcher = NumberMatcher, userValue = true, matchValues = emptyList()))
            assertTrue(
                ExistsMatcher.matches(
                    valueMatcher = NumberMatcher,
                    userValue = v("1.0.0"),
                    matchValues = emptyList()
                )
            )

            assertTrue(
                ExistsMatcher.matches(
                    valueMatcher = BooleanMatcher,
                    userValue = "abc",
                    matchValues = emptyList()
                )
            )
            assertTrue(ExistsMatcher.matches(valueMatcher = BooleanMatcher, userValue = 320, matchValues = emptyList()))
            assertTrue(
                ExistsMatcher.matches(
                    valueMatcher = BooleanMatcher,
                    userValue = true,
                    matchValues = emptyList()
                )
            )
            assertTrue(
                ExistsMatcher.matches(
                    valueMatcher = BooleanMatcher,
                    userValue = v("1.0.0"),
                    matchValues = emptyList()
                )
            )

            assertTrue(
                ExistsMatcher.matches(
                    valueMatcher = VersionMatcher,
                    userValue = "abc",
                    matchValues = emptyList()
                )
            )
            assertTrue(ExistsMatcher.matches(valueMatcher = VersionMatcher, userValue = 320, matchValues = emptyList()))
            assertTrue(
                ExistsMatcher.matches(
                    valueMatcher = VersionMatcher,
                    userValue = true,
                    matchValues = emptyList()
                )
            )
            assertTrue(
                ExistsMatcher.matches(
                    valueMatcher = VersionMatcher,
                    userValue = v("1.0.0"),
                    matchValues = emptyList()
                )
            )
        }
    }

    @Nested
    inner class RegexMatcherTest {
        @Test
        fun `if null fail`() {
            assertFalse(RegexMatcher.matches(valueMatcher = StringMatcher, userValue = null, matchValues = emptyList()))
            assertFalse(RegexMatcher.matches(valueMatcher = NumberMatcher, userValue = null, matchValues = emptyList()))
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = BooleanMatcher,
                    userValue = null,
                    matchValues = emptyList()
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = VersionMatcher,
                    userValue = null,
                    matchValues = emptyList()
                )
            )
        }

        // ======== 기본/앵커 패턴 ========
        @Test
        fun `^ (Caret) - 문자열의 시작과 일치`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abc",
                    matchValues = listOf("^ab")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "cab",
                    matchValues = listOf("^ab")
                )
            )
        }

        @Test
        fun `$ (Dollar) - 문자열의 끝과 일치`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "a a bc",
                    matchValues = listOf("bc$")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "bca",
                    matchValues = listOf("bc$")
                )
            )
        }

        // ======== 수량자 ========
        @Test
        fun `* (Asterisk) - 0회 이상 반복`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "ac",
                    matchValues = listOf("ab*c")
                )
            ) // 0회
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abc",
                    matchValues = listOf("ab*c")
                )
            ) // 1회
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abbbc",
                    matchValues = listOf("ab*c")
                )
            ) // 3회
        }

        @Test
        fun `+ (Plus) - 1회 이상 반복`() {
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "ac",
                    matchValues = listOf("ab+c")
                )
            ) // 0회
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abc",
                    matchValues = listOf("ab+c")
                )
            ) // 1회
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abbbc",
                    matchValues = listOf("ab+c")
                )
            ) // 3회
        }

        @Test
        fun `? (Question Mark) - 0 또는 1회`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "ac",
                    matchValues = listOf("ab?c")
                )
            ) // 0회
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abc",
                    matchValues = listOf("ab?c")
                )
            ) // 1회
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abbc",
                    matchValues = listOf("ab?c")
                )
            ) // 2회
        }

        @Test
        fun `{n} - 정확히 n회 반복`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abbc",
                    matchValues = listOf("ab{2}c")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abc",
                    matchValues = listOf("ab{2}c")
                )
            )
        }

        @Test
        fun `{n,} - 최소 n회 반복`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abbc",
                    matchValues = listOf("ab{2,}c")
                )
            )
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abbbbc",
                    matchValues = listOf("ab{2,}c")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abc",
                    matchValues = listOf("ab{2,}c")
                )
            )
        }

        @Test
        fun `{n,m} - n에서 m회 반복`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abbc",
                    matchValues = listOf("ab{2,4}c")
                )
            )
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abbbbc",
                    matchValues = listOf("ab{2,4}c")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abc",
                    matchValues = listOf("ab{2,4}c")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "abbbbbc",
                    matchValues = listOf("ab{2,4}c")
                )
            )
        }


        // ======== 메타 문자 ========
        @Test
        fun ` (Dot) - 어떤 문자든(줄바꿈 제외)`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "a-c",
                    matchValues = listOf("a.c")
                )
            )
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "a_c",
                    matchValues = listOf("a.c")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "ac",
                    matchValues = listOf("a.c")
                )
            )
        }

        @Test
        fun `| (Pipe) - OR 조건`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "cat",
                    matchValues = listOf("cat|dog")
                )
            )
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "dog",
                    matchValues = listOf("cat|dog")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "bird",
                    matchValues = listOf("cat|dog")
                )
            )
        }

        @Test
        fun `() (Parentheses) - 그룹화`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "I love cats",
                    matchValues = listOf("I love (cats|dogs)")
                )
            )
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "I love dogs",
                    matchValues = listOf("I love (cats|dogs)")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "I love birds",
                    matchValues = listOf("I love (cats|dogs)")
                )
            )
        }

        @Test
        fun `문자 집합`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "gray",
                    matchValues = listOf("gr[ae]y")
                )
            ) // a 또는 e
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "grey",
                    matchValues = listOf("gr[ae]y")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "groy",
                    matchValues = listOf("gr[ae]y")
                )
            )
        }

        @Test
        fun `부정 문자 집합`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "groy",
                    matchValues = listOf("gr[^ae]y")
                )
            ) // a, e 제외
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "gray",
                    matchValues = listOf("gr[^ae]y")
                )
            )
        }

        // ======== 문자 클래스 ========
        @Test
        fun `숫자`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "file-123",
                    matchValues = listOf("file-\\d+")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "file-abc",
                    matchValues = listOf("file-\\d+")
                )
            )
        }

        @Test
        fun `단어 문자 (알파벳, 숫자, _)`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "id_abc123",
                    matchValues = listOf("\\w+")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "id-!@#",
                    matchValues = listOf("^\\w+$")
                )
            ) // ^$로 전체 일치 확인
        }

        @Test
        fun `공백 문자`() {
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "hello world",
                    matchValues = listOf("hello\\sworld")
                )
            )
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "helloworld",
                    matchValues = listOf("hello\\sworld")
                )
            )
        }

        @Test
        fun `복합_정규식_패턴_테스트`() {
            // 패턴: ^(ID|USER)-(\d{3,5})\s(test|prod|dev)-[a-zA-Z_]+-v\w*!$
            // - 시작은 "ID" 또는 "USER"
            // - 하이픈(-)
            // - 3~5자리 숫자 (그룹)
            // - 공백 문자
            // - "test", "prod", "dev" 중 하나 (그룹)
            // - 하이픈(-)
            // - 알파벳과 언더스코어로만 이루어진 문자열 (1회 이상)
            // - "-v"
            // - 단어 문자 (0회 이상)
            // - "!"로 끝남
            val complexPattern = "^(ID|USER)-(\\d{3,5})\\s(test|prod|dev)-[a-zA-Z_]+-v\\w*!$"

            // 패턴에 완벽하게 일치하는 성공 케이스
            assertTrue(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "ID-12345 prod-user_name-v1a!",
                    matchValues = listOf(complexPattern)
                )
            )

            // 시작 패턴 불일치
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "WRONG-12345 prod-user_name-v1a!",
                    matchValues = listOf(complexPattern)
                )
            )
            // 숫자 개수 불일치
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "ID-12 prod-user_name-v1a!",
                    matchValues = listOf(complexPattern)
                )
            )
            // 중간 그룹 패턴 불일치
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "ID-12345 qa-user_name-v1a!",
                    matchValues = listOf(complexPattern)
                )
            )
            // 끝 패턴 불일치
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "ID-12345 prod-user-name-v1a",
                    matchValues = listOf(complexPattern)
                )
            )
            // 문자열 끝($) 불일치
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "ID-12345 prod-user_name-v1a! extra",
                    matchValues = listOf(complexPattern)
                )
            )
        }


        // ======== 예외 케이스 ========
        @Test
        fun `잘못된 정규식 pattern이면 false를 반환한다`() {
            // '['는 닫는 ']'가 없어 잘못된 패턴임
            assertFalse(
                RegexMatcher.matches(
                    valueMatcher = StringMatcher,
                    userValue = "any string",
                    matchValues = listOf("[")
                )
            )
        }


    }

    private fun v(version: String): Version = Version.parseOrNull(version)!!
}
