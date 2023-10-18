package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.Cohort
import io.hackle.sdk.core.model.Target.Key.Type.COHORT
import io.hackle.sdk.core.model.Target.Key.Type.USER_PROPERTY
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.model.condition
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class CohortConditionMatcherTest {

    @Test
    fun `when condition key type is not COHORT then throw exception`() {
        // given
        val request = experimentRequest()
        val condition = condition {
            USER_PROPERTY("age")
            IN(42)
        }

        // when
        val sut = CohortConditionMatcher(ValueOperatorMatcher(ValueOperatorMatcherFactory()))
        val actual = assertThrows<IllegalArgumentException> {
            sut.matches(request, Evaluators.context(), condition)
        }

        // then
        expectThat(actual.message)
            .isNotNull()
            .isEqualTo("Unsupported Target.Key.Type [USER_PROPERTY]")
    }

    @Test
    fun `when user cohorts is empty then return false`() {
        // given
        val request = experimentRequest(
            user = HackleUser.builder().identifier(IdentifierType.ID, "user").build()
        )
        val condition = condition {
            COHORT("COHORT")
            IN(42)
        }

        // when
        val sut = CohortConditionMatcher(ValueOperatorMatcher(ValueOperatorMatcherFactory()))
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        expectThat(actual).isFalse()
    }

    @Test
    fun `when all user cohorts do not matched then return false`() {
        // given
        val request = experimentRequest(
            user = HackleUser.builder()
                .identifier(IdentifierType.ID, "user")
                .cohort(Cohort(100))
                .cohort(Cohort(101))
                .cohort(Cohort(102))
                .build()
        )
        val condition = condition {
            COHORT("COHORT")
            IN(42)
        }

        // when
        val sut = CohortConditionMatcher(ValueOperatorMatcher(ValueOperatorMatcherFactory()))
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        expectThat(actual).isFalse()
    }

    @Test
    fun `when any of user cohorts matched then return true`() {
        // given
        val request = experimentRequest(
            user = HackleUser.builder()
                .identifier(IdentifierType.ID, "user")
                .cohort(Cohort(100))
                .cohort(Cohort(101))
                .cohort(Cohort(102))
                .build()
        )
        val condition = condition {
            COHORT("COHORT")
            IN(42, 42, 102)
        }

        // when
        val sut = CohortConditionMatcher(ValueOperatorMatcher(ValueOperatorMatcherFactory()))
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        expectThat(actual).isTrue()
    }
}
