package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

@ExtendWith(MockKExtension::class)
internal class TargetMatcherTest {

    @MockK
    private lateinit var conditionMatcher: ConditionMatcher

    @MockK
    private lateinit var conditionMatcherFactory: ConditionMatcherFactory

    @InjectMockKs
    private lateinit var sut: TargetMatcher

    @BeforeEach
    fun beforeEach() {
        every { conditionMatcherFactory.getMatcher(any()) } returns conditionMatcher
    }

    @DisplayName("matches()")
    @Nested
    inner class MatchesTest {
        @Test
        fun `타겟의 모든 조건이 일치하면 match true`() {
            // given
            val target = Target(
                conditions = listOf(
                    condition(true),
                    condition(true),
                    condition(true),
                    condition(true),
                    condition(true),
                )
            )

            // when
            val actual = sut.matches(mockk(), mockk(), target)

            // then
            assertTrue(actual)
            verify(exactly = 5) {
                conditionMatcher.matches(any(), any(), any())
            }
        }

        @Test
        fun `타겟의 조건중 하나라도 일치하지 않으면 match false`() {
            // given
            val target = Target(
                conditions = listOf(
                    condition(true),
                    condition(true),
                    condition(true),
                    condition(false),
                    condition(true),
                    condition(true),
                    condition(true),
                )
            )

            // when
            val actual = sut.matches(mockk(), mockk(), target)

            // then
            assertFalse(actual)
            verify(exactly = 4) {
                conditionMatcher.matches(any(), any(), any())
            }
        }
    }


    @DisplayName("anyMatches()")
    @Nested
    inner class AnyMatchesTest {


        @Test
        fun `when empty then returns true`() {
            expectThat(sut.anyMatches(mockk(), mockk(), emptyList())).isTrue()
        }


        @Test
        fun `when any of target matched then return true`() {
            expectThat(
                sut.anyMatches(
                    mockk(), mockk(), listOf(
                        Target(listOf(condition(false))),
                        Target(listOf(condition(true))),
                    )
                )
            ).isTrue()
        }

        @Test
        fun `when all targets not matched then returns false`() {
            expectThat(
                sut.anyMatches(
                    mockk(), mockk(), listOf(
                        Target(listOf(condition(false))),
                        Target(listOf(condition(false))),
                        Target(listOf(condition(false))),
                        Target(listOf(condition(false))),
                        Target(listOf(condition(false))),
                    )
                )
            ).isFalse()
        }
    }

    private fun condition(isMatch: Boolean): Target.Condition {
        val condition = mockk<Target.Condition> {
            every { key } returns mockk {
                every { type } returns mockk()
            }
        }
        every { conditionMatcher.matches(any(), any(), condition) } returns isMatch
        return condition
    }
}