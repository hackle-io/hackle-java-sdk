package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target.Match.Operator.*
import org.junit.jupiter.api.Assertions
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
            assertTrue(sut.matches(userValue = "abc", matchValue = "abc"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "abc1"))
        }

        @Test
        fun `number`() {
            assertTrue(sut.matches(userValue = 320, matchValue = 320))
            assertTrue(sut.matches(userValue = 320.0, matchValue = 320))
            assertTrue(sut.matches(userValue = 320.1, matchValue = BigDecimal("320.10")))
            assertTrue(sut.matches(userValue = BigDecimal("320.00"), matchValue = 320))
            assertFalse(sut.matches(userValue = BigDecimal("320.01"), matchValue = 320))
            assertFalse(sut.matches(userValue = 321, matchValue = 320))
        }

        @Test
        fun `boolean`() {
            assertTrue(sut.matches(userValue = true, matchValue = true))
            assertTrue(sut.matches(userValue = false, matchValue = false))
            assertFalse(sut.matches(userValue = true, matchValue = false))
            assertFalse(sut.matches(userValue = false, matchValue = true))
        }
    }

    @Nested
    inner class ContainsMatcherTest {

        private val sut = ContainsMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(userValue = "abc", matchValue = "abc"))
            assertTrue(sut.matches(userValue = "abc", matchValue = "a"))
            assertTrue(sut.matches(userValue = "abc", matchValue = "b"))
            assertTrue(sut.matches(userValue = "abc", matchValue = "c"))
            assertTrue(sut.matches(userValue = "abc", matchValue = "ab"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "ac"))
            assertFalse(sut.matches(userValue = "a", matchValue = "ab"))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(userValue = 1, matchValue = 1))
            assertFalse(sut.matches(userValue = 11, matchValue = 1))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(userValue = true, matchValue = true))
            assertFalse(sut.matches(userValue = false, matchValue = false))
            assertFalse(sut.matches(userValue = true, matchValue = false))
            assertFalse(sut.matches(userValue = false, matchValue = true))
        }
    }

    @Nested
    inner class StartsWithMatcherTest {

        private val sut = StartsWithMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(userValue = "abc", matchValue = "abc"))
            assertTrue(sut.matches(userValue = "abc", matchValue = "a"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "b"))
            assertFalse(sut.matches(userValue = "a", matchValue = "ab"))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(userValue = 1, matchValue = 1))
            assertFalse(sut.matches(userValue = 11, matchValue = 1))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(userValue = true, matchValue = true))
            assertFalse(sut.matches(userValue = false, matchValue = false))
            assertFalse(sut.matches(userValue = true, matchValue = false))
            assertFalse(sut.matches(userValue = false, matchValue = true))
        }
    }

    @Nested
    inner class EndWithMatcherTest {

        private val sut = EndsWithMatcher

        @Test
        fun `string`() {
            assertTrue(sut.matches(userValue = "abc", matchValue = "abc"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "a"))
            assertTrue(sut.matches(userValue = "abc", matchValue = "c"))
            assertTrue(sut.matches(userValue = "abc", matchValue = "bc"))
            assertFalse(sut.matches(userValue = "a", matchValue = "ab"))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(userValue = 1, matchValue = 1))
            assertFalse(sut.matches(userValue = 11, matchValue = 1))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(userValue = true, matchValue = true))
            assertFalse(sut.matches(userValue = false, matchValue = false))
            assertFalse(sut.matches(userValue = true, matchValue = false))
            assertFalse(sut.matches(userValue = false, matchValue = true))
        }
    }

    @Nested
    inner class GreaterThanMatcherTest {

        private val sut = GreaterThanMatcher

        @Test
        fun `string`() {
            assertFalse(sut.matches(userValue = "abc", matchValue = "abc"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "a"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "c"))
        }

        @Test
        fun `number`() {
            assertTrue(sut.matches(userValue = 1.001, matchValue = 1))
            assertTrue(sut.matches(userValue = 2, matchValue = 1))
            assertFalse(sut.matches(userValue = 1, matchValue = 1))
            assertFalse(sut.matches(userValue = 0.999, matchValue = 1))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(userValue = true, matchValue = true))
            assertFalse(sut.matches(userValue = false, matchValue = false))
            assertFalse(sut.matches(userValue = true, matchValue = false))
            assertFalse(sut.matches(userValue = false, matchValue = true))
        }
    }

    @Nested
    inner class GreaterThanOrEqualToMatcherTest {

        private val sut = GreaterThanOrEqualToMatcher

        @Test
        fun `string`() {
            assertFalse(sut.matches(userValue = "abc", matchValue = "abc"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "a"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "c"))
        }

        @Test
        fun `number`() {
            assertTrue(sut.matches(userValue = 1.001, matchValue = 1))
            assertTrue(sut.matches(userValue = 2, matchValue = 1))
            assertTrue(sut.matches(userValue = 1, matchValue = 1))
            assertFalse(sut.matches(userValue = 0.999, matchValue = 1))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(userValue = true, matchValue = true))
            assertFalse(sut.matches(userValue = false, matchValue = false))
            assertFalse(sut.matches(userValue = true, matchValue = false))
            assertFalse(sut.matches(userValue = false, matchValue = true))
        }
    }

    @Nested
    inner class LessThanMatcherTest {

        private val sut = LessThanMatcher

        @Test
        fun `string`() {
            assertFalse(sut.matches(userValue = "abc", matchValue = "abc"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "a"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "c"))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(userValue = 1.001, matchValue = 1))
            assertFalse(sut.matches(userValue = 2, matchValue = 1))
            assertFalse(sut.matches(userValue = 1, matchValue = 1))
            assertTrue(sut.matches(userValue = 0.999, matchValue = 1))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(userValue = true, matchValue = true))
            assertFalse(sut.matches(userValue = false, matchValue = false))
            assertFalse(sut.matches(userValue = true, matchValue = false))
            assertFalse(sut.matches(userValue = false, matchValue = true))
        }
    }

    @Nested
    inner class LessThanOrEqualToMatcherTest {

        private val sut = LessThanOrEqualToMatcher

        @Test
        fun `string`() {
            assertFalse(sut.matches(userValue = "abc", matchValue = "abc"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "a"))
            assertFalse(sut.matches(userValue = "abc", matchValue = "c"))
        }

        @Test
        fun `number`() {
            assertFalse(sut.matches(userValue = 1.001, matchValue = 1))
            assertFalse(sut.matches(userValue = 2, matchValue = 1))
            assertTrue(sut.matches(userValue = 1, matchValue = 1))
            assertTrue(sut.matches(userValue = 0.999, matchValue = 1))
        }

        @Test
        fun `boolean`() {
            assertFalse(sut.matches(userValue = true, matchValue = true))
            assertFalse(sut.matches(userValue = false, matchValue = false))
            assertFalse(sut.matches(userValue = true, matchValue = false))
            assertFalse(sut.matches(userValue = false, matchValue = true))
        }
    }
}