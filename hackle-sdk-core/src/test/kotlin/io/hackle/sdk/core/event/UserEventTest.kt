package io.hackle.sdk.core.event

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.user.HackleUser
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

internal class UserEventTest {

    @Nested
    inner class ExposureTest {

        @Test
        fun `parameterConfigurationId 를 속성으로 설정한다`() {
            // given
            val parameterConfiguration = ParameterConfiguration(42, emptyList())
            val evaluation = Evaluation(320, "B", DecisionReason.TRAFFIC_ALLOCATED, parameterConfiguration)
            val experiment = mockk<Experiment>()
            val user = HackleUser.of("test_id")

            // when
            val actual = UserEvent.exposure(experiment, user, evaluation)

            // then
            expectThat(actual)
                .isA<UserEvent.Exposure>()
                .get { properties["\$parameterConfigurationId"] }
                .isNotNull()
                .isEqualTo(42L)
        }

    }
}