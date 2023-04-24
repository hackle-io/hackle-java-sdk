package io.hackle.sdk.core.evaluation.evaluator.remoteconfig

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.evaluation.target.RemoteConfigParameterTargetRuleDeterminer
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.model.ValueType.*
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class RemoteConfigEvaluatorTest {

    @MockK
    private lateinit var targetRuleDeterminer: RemoteConfigParameterTargetRuleDeterminer

    @InjectMockKs
    private lateinit var sut: RemoteConfigEvaluator<Any>

    @BeforeEach
    fun beforeEach() {
        every { targetRuleDeterminer.determineTargetRuleOrNull(any(), any()) } returns null
    }

    @Test
    fun `supports`() {
        assertTrue(sut.supports(mockk<RemoteConfigRequest<Any>>()))
        assertFalse(sut.supports(mockk<ExperimentRequest>()))
    }

    @Nested
    inner class EvaluateTest {

        @Test
        fun `순환 호출`() {
            val request = remoteConfigRequest<Any>()
            val context = Evaluators.context()
            context.add(request)

            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context)
            }

            expectThat(exception.message)
                .isNotNull()
                .startsWith("Circular evaluation has occurred")
        }

        @Test
        fun `식별자가 없는 경우`() {
            // given
            val parameter = parameter(
                type = STRING,
                identifierType = "customId",
                defaultValue = RemoteConfigParameter.Value(43, "hello value")
            )

            val request = remoteConfigRequest<Any>(parameter = parameter, defaultValue = "default")

            // when
            val actual = sut.evaluate(request, Evaluators.context())

            // then
            expectThat(actual) {
                get { reason } isEqualTo DecisionReason.IDENTIFIER_NOT_FOUND
                get { value } isEqualTo "default"
                get { properties } isEqualTo mapOf(
                    "requestValueType" to "STRING",
                    "requestDefaultValue" to "default",
                    "returnValue" to "default",
                )
            }
        }

        @Test
        fun `TargetRule 에 해당하는 경우`() {
            // given
            val targetRule = RemoteConfigParameter.TargetRule(
                "target_rule_key",
                "target_rule_name",
                mockk(),
                42,
                RemoteConfigParameter.Value(320, "targetRuleValue")
            )
            val parameter = parameter(
                type = STRING,
                targetRules = listOf(targetRule),
                defaultValue = RemoteConfigParameter.Value(43, "hello value")
            )

            every { targetRuleDeterminer.determineTargetRuleOrNull(any(), any()) } returns targetRule

            val request = remoteConfigRequest(parameter = parameter, defaultValue = "default")

            // when
            val actual = sut.evaluate(request, Evaluators.context())

            // then
            expectThat(actual) {
                get { reason } isEqualTo DecisionReason.TARGET_RULE_MATCH
                get { valueId } isEqualTo 320
                get { value } isEqualTo "targetRuleValue"
                get { properties } isEqualTo mapOf(
                    "requestValueType" to "STRING",
                    "requestDefaultValue" to "default",
                    "returnValue" to "targetRuleValue",
                    "targetRuleKey" to "target_rule_key",
                    "targetRuleName" to "target_rule_name",
                )
            }
        }

        @Test
        fun `TargetRule 에 매치되지 않는경우`() {
            // given
            val targetRule = mockk<RemoteConfigParameter.TargetRule> {
                every { value } returns RemoteConfigParameter.Value(320, "targetRuleValue")
            }
            val parameter = parameter(
                type = STRING,
                targetRules = listOf(targetRule),
                defaultValue = RemoteConfigParameter.Value(43, "hello value")
            )

            val request = remoteConfigRequest(parameter = parameter, defaultValue = "default")

            // when
            val actual = sut.evaluate(request, Evaluators.context())

            // then
            expectThat(actual) {
                get { reason } isEqualTo DecisionReason.DEFAULT_RULE
                get { valueId } isEqualTo 43
                get { value } isEqualTo "hello value"
                get { properties } isEqualTo mapOf(
                    "requestValueType" to "STRING",
                    "requestDefaultValue" to "default",
                    "returnValue" to "hello value",
                )
            }
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
                defaultValue = RemoteConfigParameter.Value(43, matchValue)
            )


            val request =
                remoteConfigRequest(parameter = parameter, requiredType = requiredType, defaultValue = defaultValue)

            val actual = sut.evaluate(request, Evaluators.context())

            if (isMatch) {
                expectThat(actual) {
                    get { valueId } isEqualTo 43
                    get { value } isEqualTo matchValue
                    get { reason } isEqualTo DecisionReason.DEFAULT_RULE
                }
            } else {
                expectThat(actual) {
                    get { valueId }.isNull()
                    get { value } isEqualTo defaultValue
                    get { reason } isEqualTo DecisionReason.TYPE_MISMATCH
                }
            }
        }

        private fun parameter(
            id: Long = 42,
            key: String = "test_parameter_key",
            type: ValueType,
            identifierType: String = "\$id",
            targetRules: List<RemoteConfigParameter.TargetRule> = emptyList(),
            defaultValue: RemoteConfigParameter.Value
        ): RemoteConfigParameter {
            return RemoteConfigParameter(id, key, type, identifierType, targetRules, defaultValue)
        }
    }


}