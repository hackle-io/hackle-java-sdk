package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.Cohort
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.COHORT
import io.hackle.sdk.core.model.Target.Key.Type.USER_PROPERTY
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.model.Target.Match.Type.MATCH
import io.hackle.sdk.core.model.Target.Match.Type.NOT_MATCH
import io.hackle.sdk.core.model.ValueType.NUMBER
import io.hackle.sdk.core.model.condition
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

class CohortConditionMatcherTest {

    private val sut = CohortConditionMatcher(ValueOperatorMatcher(ValueOperatorMatcherFactory()))

    @Test
    fun `when condition key type is not COHORT then throw exception`() {
        // given
        val request = experimentRequest()
        val condition = condition {
            USER_PROPERTY("age")
            IN(42)
        }

        // when
        val actual = assertThrows<IllegalArgumentException> {
            sut.matches(request, Evaluators.context(), condition)
        }

        // then
        expectThat(actual.message)
            .isNotNull()
            .isEqualTo("Unsupported Target.Key.Type [USER_PROPERTY]")
    }


    @Test
    fun `matches`() {

        // UserCohort[] 는 Cohort[1] 중 하나
        verify(MATCH, listOf(), listOf(1), false)

        // UserCohort[1] 는 Cohort[1] 중 하나
        verify(MATCH, listOf(1), listOf(1), true)

        // UserCohort[1] 는 Cohort[1, 2] 중 하나
        verify(MATCH, listOf(1), listOf(1, 2), true)

        // UserCohort[2] 는 Cohort[1, 2] 중 하나
        verify(MATCH, listOf(2), listOf(1, 2), true)

        // UserCohort[1] 는 Cohort[2] 중 하나
        verify(MATCH, listOf(1), listOf(2), false)

        // UserCohort[1] 는 Cohort[2, 3] 중 하나
        verify(MATCH, listOf(1), listOf(2, 3), false)

        // UserCohort[1, 2] 는 Cohort[1] 중 하나
        verify(MATCH, listOf(1, 2), listOf(1), true)

        // UserCohort[1, 2] 는 Cohort[2] 중 하나
        verify(MATCH, listOf(1, 2), listOf(2), true)

        // UserCohort[1, 2] 는 Cohort[3] 중 하나
        verify(MATCH, listOf(1, 2), listOf(3), false)

        // UserCohort[1, 2] 는 Cohort[1, 2] 중 하나
        verify(MATCH, listOf(1, 2), listOf(1, 2), true)

        // UserCohort[1, 2] 는 Cohort[1, 3] 중 하나
        verify(MATCH, listOf(1, 2), listOf(1, 3), true)

        // UserCohort[1, 2] 는 Cohort[2, 3] 중 하나
        verify(MATCH, listOf(1, 2), listOf(2, 3), true)

        // UserCohort[1, 2] 는 Cohort[3, 2] 중 하나
        verify(MATCH, listOf(1, 2), listOf(3, 2), true)

        // UserCohort[1, 2] 는 Cohort[3, 4] 중 하나
        verify(MATCH, listOf(1, 2), listOf(3, 4), false)


        // UserCohort[] 는 Cohort[1] 중 하나가 아닌
        verify(NOT_MATCH, listOf(), listOf(1), true)

        // UserCohort[1] 는 Cohort[1] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1), listOf(1), false)

        // UserCohort[1] 는 Cohort[1, 2] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1), listOf(1, 2), false)

        // UserCohort[2] 는 Cohort[1, 2] 중 하나가 아닌
        verify(NOT_MATCH, listOf(2), listOf(1, 2), false)

        // UserCohort[1] 는 Cohort[2] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1), listOf(2), true)

        // UserCohort[1] 는 Cohort[2, 3] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1), listOf(2, 3), true)

        // UserCohort[1, 2] 는 Cohort[1] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1, 2), listOf(1), false)

        // UserCohort[1, 2] 는 Cohort[2] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1, 2), listOf(2), false)

        // UserCohort[1, 2] 는 Cohort[3] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1, 2), listOf(3), true)

        // UserCohort[1, 2] 는 Cohort[1, 2] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1, 2), listOf(1, 2), false)

        // UserCohort[1, 2] 는 Cohort[1, 3] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1, 2), listOf(1, 3), false)

        // UserCohort[1, 2] 는 Cohort[2, 3] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1, 2), listOf(2, 3), false)

        // UserCohort[1, 2] 는 Cohort[3, 2] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1, 2), listOf(3, 2), false)

        // UserCohort[1, 2] 는 Cohort[3, 4] 중 하나가 아닌
        verify(NOT_MATCH, listOf(1, 2), listOf(3, 4), true)

    }

    private fun verify(type: Target.Match.Type, userCohorts: List<Long>, cohorts: List<Long>, expected: Boolean) {
        val request = experimentRequest(
            user = HackleUser.builder()
                .identifier(IdentifierType.ID, "user")
                .cohorts(userCohorts.map { Cohort(it) })
                .build()
        )

        val condition = condition {
            key(COHORT, "COHORT")
            match(type, IN, NUMBER, cohorts)
        }

        val actual = sut.matches(request, Evaluators.context(), condition)

        assertEquals(expected, actual)
    }
}
