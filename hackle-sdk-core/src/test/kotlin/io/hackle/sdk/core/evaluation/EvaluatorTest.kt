package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory
import io.hackle.sdk.core.evaluation.target.RemoteConfigParameterTargetRuleDeterminer
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.RemoteConfigParameter.Value
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.model.ValueType.*
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull


@ExtendWith(MockKExtension::class)
internal class EvaluatorTest {

    @MockK
    private lateinit var evaluationFlowFactory: EvaluationFlowFactory

    @InjectMockKs
    private lateinit var sut: Evaluator

    @Nested
    inner class ExperimentEvaluateTest {

        @Test
        fun `evaluationFlowFactory에서 ExperimentType으로 Flow를 가져와서 평가한다`() {
            // given

            val evaluation = Evaluation(430, "B", DecisionReason.TRAFFIC_ALLOCATED, null)

            val evaluationFlow = mockk<EvaluationFlow> {
                every { evaluate(any(), any(), any(), any()) } returns evaluation
            }

            every { evaluationFlowFactory.getFlow(any()) } returns evaluationFlow

            val workspace = mockk<Workspace>()
            val experiment = mockk<Experiment> { every { type } returns Experiment.Type.AB_TEST }
            val user = HackleUser.of("test_id")

            // when
            val actual = sut.evaluate(workspace, experiment, user, "J")

            // then
            expectThat(actual) isEqualTo evaluation
            verify(exactly = 1) {
                evaluationFlow.evaluate(workspace, experiment, user, "J")
            }
        }

    }

    @Nested
    inner class RemoteConfigEvaluateTest {

        @MockK
        private lateinit var remoteConfigParameterTargetRuleDeterminer: RemoteConfigParameterTargetRuleDeterminer

        @BeforeEach
        fun beforeEach() {
            every { evaluationFlowFactory.remoteConfigParameterTargetRuleDeterminer } returns remoteConfigParameterTargetRuleDeterminer
            every {
                remoteConfigParameterTargetRuleDeterminer.determineTargetRuleOrNull(
                    any(),
                    any(),
                    any()
                )
            } returns null
        }

        @Test
        fun `식별자가 없는 경우`() {
            // given
            val parameter = parameter(
                type = STRING,
                identifierType = "customId",
                defaultValue = Value(43, "hello value")
            )

            // when
            val actual = sut.evaluate(mockk(), parameter, HackleUser.of("test"), STRING, "default")

            // then
            expectThat(actual) isEqualTo RemoteConfigEvaluation(
                null, "default", IDENTIFIER_NOT_FOUND, mapOf(
                    "requestValueType" to "STRING",
                    "requestDefaultValue" to "default",
                    "returnValue" to "default",
                )
            )
        }

        @Test
        fun `TargetRule 에 해당하는 경우`() {
            // given
            val targetRule = mockk<RemoteConfigParameter.TargetRule> {
                every { key } returns "target_rule_key"
                every { name } returns "target_rule_name"
                every { value } returns Value(320, "targetRuleValue")
            }
            val parameter = parameter(
                type = STRING,
                targetRules = listOf(targetRule),
                defaultValue = Value(43, "hello value")
            )

            every {
                remoteConfigParameterTargetRuleDeterminer.determineTargetRuleOrNull(any(), parameter, any())
            } returns targetRule

            // when
            val actual = sut.evaluate(mockk(), parameter, HackleUser.of("test"), STRING, "default")

            // then
            expectThat(actual) isEqualTo RemoteConfigEvaluation(
                320, "targetRuleValue", TARGET_RULE_MATCH, mapOf(
                    "requestValueType" to "STRING",
                    "requestDefaultValue" to "default",
                    "returnValue" to "targetRuleValue",
                    "targetRuleKey" to "target_rule_key",
                    "targetRuleName" to "target_rule_name",
                )
            )
        }

        @Test
        fun `TargetRule 에 매치되지 않는경우`() {
            // given
            val targetRule = mockk<RemoteConfigParameter.TargetRule> {
                every { value } returns Value(320, "targetRuleValue")
            }
            val parameter = parameter(
                type = STRING,
                targetRules = listOf(targetRule),
                defaultValue = Value(43, "hello value")
            )

            // when
            val actual = sut.evaluate(mockk(), parameter, HackleUser.of("test"), STRING, "default")

            // then
            expectThat(actual) isEqualTo RemoteConfigEvaluation(
                43, "hello value", DEFAULT_RULE, mapOf(
                    "requestValueType" to "STRING",
                    "requestDefaultValue" to "default",
                    "returnValue" to "hello value",
                )
            )
        }

        @Test
        fun `type match`() {
            verityTypeMatch(STRING, "match_string", "default_string", true)
            verityTypeMatch(STRING, "", "default_string", true)
            verityTypeMatch(STRING, 0, "default_string", false)
            verityTypeMatch(STRING, 1, "default_string", false)
            verityTypeMatch(STRING, false, "default_string", false)
            verityTypeMatch(STRING, true, "default_string", false)

            verityTypeMatch(NUMBER, 0, 999, true)
            verityTypeMatch(NUMBER, 1, 999, true)
            verityTypeMatch(NUMBER, -1, 999, true)
            verityTypeMatch(NUMBER, 0L, 999, true)
            verityTypeMatch(NUMBER, 1L, 999, true)
            verityTypeMatch(NUMBER, -1L, 999, true)
            verityTypeMatch(NUMBER, 0.0, 999, true)
            verityTypeMatch(NUMBER, 1.0, 999, true)
            verityTypeMatch(NUMBER, -1.0, 999, true)
            verityTypeMatch(NUMBER, 1.1, 999, true)
            verityTypeMatch(NUMBER, "1", 999, false)
            verityTypeMatch(NUMBER, "0", 999, false)
            verityTypeMatch(NUMBER, true, 999, false)
            verityTypeMatch(NUMBER, false, 999, false)

            verityTypeMatch(BOOLEAN, true, false, true)
            verityTypeMatch(BOOLEAN, false, true, true)
            verityTypeMatch(BOOLEAN, 0, true, false)
            verityTypeMatch(BOOLEAN, 1, false, false)

            verityTypeMatch(VERSION, "1.0.0", "default", false)
            verityTypeMatch(JSON, "{}", "default", false)
        }

        private fun verityTypeMatch(requiredType: ValueType, matchValue: Any, defaultValue: Any, isMatch: Boolean) {
            val parameter = parameter(
                type = STRING,
                defaultValue = Value(43, matchValue)
            )

            val actual = sut.evaluate(mockk(), parameter, HackleUser.of("test"), requiredType, defaultValue)

            if (isMatch) {
                expectThat(actual) {
                    get { valueId } isEqualTo 43
                    get { value } isEqualTo matchValue
                    get { reason } isEqualTo DEFAULT_RULE
                }
            } else {
                expectThat(actual) {
                    get { valueId }.isNull()
                    get { value } isEqualTo defaultValue
                    get { reason } isEqualTo TYPE_MISMATCH
                }
            }
        }

        private fun parameter(
            id: Long = 42,
            key: String = "test_parameter_key",
            type: ValueType,
            identifierType: String = "\$id",
            targetRules: List<RemoteConfigParameter.TargetRule> = emptyList(),
            defaultValue: Value
        ): RemoteConfigParameter {
            return RemoteConfigParameter(id, key, type, identifierType, targetRules, defaultValue)
        }
    }
}