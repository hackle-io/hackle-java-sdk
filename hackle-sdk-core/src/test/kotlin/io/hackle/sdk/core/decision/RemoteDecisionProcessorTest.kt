package io.hackle.sdk.core.decision

import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.evaluation.EvaluateProcessor
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateRequest
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.model.Variation
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.RemoteConfigs
import io.hackle.sdk.core.support.VariationKey
import io.hackle.sdk.core.support.Workspaces
import io.hackle.sdk.core.support.parameterConfiguration
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.hackle.sdk.core.workspace.evaluation.WorkspaceEvaluationFetcher
import io.hackle.sdk.core.workspace.evaluation.entity.ExperimentRemoteEvaluateResult
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
class RemoteDecisionProcessorTest {

    @MockK
    private lateinit var workspaceFetcher: WorkspaceEvaluationFetcher

    @MockK
    private lateinit var evaluateProcessor: EvaluateProcessor

    @InjectMockKs
    private lateinit var sut: RemoteDecisionProcessor

    @Nested
    inner class ExperimentTest {
        @Test
        fun `Workspace를 가져오지 못하면 Control Variation으로 결정하고 평가를 하지 않는다`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            every { workspaceFetcher.workspace(any()) } returns null

            // when
            val actual = sut.experiment(42, user)

            //then
            val decision = Decision.of(VariationKey.CONTROL, DecisionReason.SDK_NOT_READY)
            expectThat(actual) isEqualTo decision
            verify { evaluateProcessor wasNot Called }
        }

        @Test
        fun `experimentKey에 해당하는 experiment가 없으면 Control Variation으로 결정하고 평가를 하지 않는다`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            val workspace = Workspaces.evaluation()
            every { workspaceFetcher.workspace(any()) } returns workspace

            // when
            val actual = sut.experiment(42, user)

            //then
            val decision = Decision.of(VariationKey.CONTROL, DecisionReason.EXPERIMENT_NOT_FOUND)
            expectThat(actual) isEqualTo decision
            verify { evaluateProcessor wasNot Called }
        }

        @Test
        fun `평가 결과로 결정한다`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            val parameterConfiguration = parameterConfiguration(parameters = mapOf("hello" to "SDK"))
            val variation = Experiments.variation(key = "B", parameterConfiguration = parameterConfiguration)
            val experiment = Experiments.remoteResult(
                key = 42,
                variation = variation,
                reason = DecisionReason.TRAFFIC_ALLOCATED
            )
            val workspace = Workspaces.evaluation(
                experiments = listOf(experiment)
            )
            every { workspaceFetcher.workspace(any()) } returns workspace

            val response = Experiments.response(
                user = user,
                workspace = workspace,
                evaluation = experiment.toEvaluation()
            )
            every { evaluateProcessor.experiment(any()) } returns response

            // when
            val actual = sut.experiment(42, user)

            // then
            val decision =
                Decision.of(VariationKey.B, DecisionReason.TRAFFIC_ALLOCATED, parameterConfiguration, experiment)
            expectThat(actual) isEqualTo decision
        }
    }

    @Nested
    inner class ExperimentsTest {
        @Test
        fun `Workspace 를 가져오지 못하면 비어있는 map 을 리턴한다`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            every { workspaceFetcher.workspace(any()) } returns null

            // when
            val actual = sut.experiments(user)

            // then
            expectThat(actual).hasSize(0)
        }

        @Test
        fun `모든 실험에 대한 분배 결과를 리턴한다`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()

            val config42 = parameterConfiguration(id = 42)
            val config43 = parameterConfiguration(id = 43)
            val experiments = listOf(
                experiment(
                    id = 1,
                    key = 2,
                    reason = DecisionReason.EXPERIMENT_DRAFT,
                    variation = Experiments.variation(id = 3, key = "A", parameterConfiguration = config42)
                ),
                experiment(
                    id = 4,
                    key = 5,
                    reason = DecisionReason.EXPERIMENT_COMPLETED,
                    variation = Experiments.variation(id = 6, key = "B", parameterConfiguration = config43)
                ),
                experiment(
                    id = 7,
                    key = 8,
                    reason = DecisionReason.OVERRIDDEN,
                    variation = Experiments.variation(id = 9, key = "A")
                ),
                experiment(
                    id = 10,
                    key = 11,
                    reason = DecisionReason.TRAFFIC_ALLOCATED,
                    variation = Experiments.variation(id = 12, key = "B")
                ),
                experiment(
                    id = 13,
                    key = 14,
                    reason = DecisionReason.NOT_IN_EXPERIMENT_TARGET,
                    variation = Experiments.variation(id = 15, key = "A")
                )
            )
            val workspace = Workspaces.evaluation(experiments = experiments)

            every { workspaceFetcher.workspace(any()) } returns workspace

            // when
            val actual = sut.experiments(user)

            // then
            expectThat(actual).hasSize(5)
        }

        private fun experiment(
            id: Long,
            key: Long,
            reason: DecisionReason,
            variation: Variation,
        ): ExperimentRemoteEvaluateResult {
            val experiment = Experiments.remoteResult(
                id = id,
                key = key,
                variation = variation,
                reason = reason
            )
            every { evaluateProcessor.experiment(any()) } answers {
                val request = firstArg<ExperimentEvaluateRequest>()
                Experiments.response(
                    user = request.user,
                    workspace = request.workspace,
                    evaluation = experiment.toEvaluation()
                )
            }
            return experiment
        }
    }

    @Nested
    inner class FeatureFlagTest {
        @Test
        fun `Workspace를 가져오지 못하면 평가 없이 off로 결정`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            every { workspaceFetcher.workspace(any()) } returns null

            // when
            val actual = sut.featureFlag(42, user)

            //then
            val decision = FeatureFlagDecision.off(DecisionReason.SDK_NOT_READY)
            expectThat(actual) isEqualTo decision
            verify { evaluateProcessor wasNot Called }
        }

        @Test
        fun `featureFlagKey에 해당하는 featureFlag가 없으면 평가 없이 off로 결정`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            val workspace = Workspaces.evaluation()
            every { workspaceFetcher.workspace(any()) } returns workspace

            // when
            val actual = sut.featureFlag(42, user)

            //then
            val decision = FeatureFlagDecision.off(DecisionReason.FEATURE_FLAG_NOT_FOUND)
            expectThat(actual) isEqualTo decision
            verify { evaluateProcessor wasNot Called }
        }

        @Test
        fun `평가 결과 Control 그룹이면 off로 결정한다`() {
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            val parameterConfiguration = parameterConfiguration(parameters = mapOf("hello" to "SDK"))
            val variation = Experiments.variation(key = "A", parameterConfiguration = parameterConfiguration)
            val featureFlag = Experiments.remoteResult(
                key = 42,
                type = Experiment.Type.FEATURE_FLAG,
                variation = variation,
                reason = DecisionReason.DEFAULT_RULE
            )
            val workspace = Workspaces.evaluation(
                featureFlags = listOf(featureFlag)
            )
            every { workspaceFetcher.workspace(any()) } returns workspace

            val response = Experiments.response(
                user = user,
                workspace = workspace,
                evaluation = featureFlag.toEvaluation()
            )
            every { evaluateProcessor.experiment(any()) } returns response

            // when
            val actual = sut.featureFlag(42, user)

            // then
            val decision =
                FeatureFlagDecision.off(DecisionReason.DEFAULT_RULE, parameterConfiguration, featureFlag)
            expectThat(actual) isEqualTo decision
        }

        @Test
        fun `평가 결과가 Control 그룹이 아니면 on으로 결정한다 `() {
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            val parameterConfiguration = parameterConfiguration(parameters = mapOf("hello" to "SDK"))
            val variation = Experiments.variation(key = "B", parameterConfiguration = parameterConfiguration)
            val featureFlag = Experiments.remoteResult(
                key = 42,
                type = Experiment.Type.FEATURE_FLAG,
                variation = variation,
                reason = DecisionReason.DEFAULT_RULE
            )
            val workspace = Workspaces.evaluation(
                featureFlags = listOf(featureFlag)
            )
            every { workspaceFetcher.workspace(any()) } returns workspace

            val response = Experiments.response(
                user = user,
                workspace = workspace,
                evaluation = featureFlag.toEvaluation()
            )
            every { evaluateProcessor.experiment(any()) } returns response

            // when
            val actual = sut.featureFlag(42, user)

            // then
            val decision =
                FeatureFlagDecision.on(DecisionReason.DEFAULT_RULE, parameterConfiguration, featureFlag)
            expectThat(actual) isEqualTo decision
        }
    }

    @Nested
    inner class FeatureFlagsTest {

        @Test
        fun `Workspace 를 가져오지 못하면 비어있는 map 을 리턴한다`() {
            // given
            every { workspaceFetcher.workspace(any()) } returns null
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()

            // when
            val actual = sut.featureFlags(user)

            // then
            expectThat(actual).hasSize(0)
        }

        @Test
        fun `모든 기능플래그 대한 분배 결과를 리턴한다`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()

            val config42 = parameterConfiguration(id = 42)
            val config43 = parameterConfiguration(id = 43)
            val featureFlags = listOf(
                featureFlag(
                    id = 1,
                    key = 2,
                    reason = DecisionReason.FEATURE_FLAG_INACTIVE,
                    variation = Experiments.variation(id = 3, key = "A", parameterConfiguration = config42)
                ),
                featureFlag(
                    id = 4,
                    key = 5,
                    reason = DecisionReason.INDIVIDUAL_TARGET_MATCH,
                    variation = Experiments.variation(id = 6, key = "B", parameterConfiguration = config43)
                ),
                featureFlag(
                    id = 7,
                    key = 8,
                    reason = DecisionReason.TARGET_RULE_MATCH,
                    variation = Experiments.variation(id = 9, key = "A")
                ),
                featureFlag(
                    id = 10,
                    key = 11,
                    reason = DecisionReason.DEFAULT_RULE,
                    variation = Experiments.variation(id = 12, key = "B")
                ),
                featureFlag(
                    id = 13,
                    key = 14,
                    reason = DecisionReason.DEFAULT_RULE,
                    variation = Experiments.variation(id = 15, key = "A")
                )
            )
            val workspace = Workspaces.evaluation(featureFlags = featureFlags)

            every { workspaceFetcher.workspace(any()) } returns workspace

            // when
            val actual = sut.featureFlags(user)

            // then
            expectThat(actual).hasSize(5)
        }

        private fun featureFlag(
            id: Long,
            key: Long,
            reason: DecisionReason,
            variation: Variation,
        ): ExperimentRemoteEvaluateResult {
            val experiment = Experiments.remoteResult(
                id = id,
                key = key,
                type = Experiment.Type.FEATURE_FLAG,
                variation = variation,
                reason = reason
            )
            every { evaluateProcessor.experiment(any()) } answers {
                val request = firstArg<ExperimentEvaluateRequest>()
                Experiments.response(
                    user = request.user,
                    workspace = request.workspace,
                    evaluation = experiment.toEvaluation()
                )
            }
            return experiment
        }
    }

    @Nested
    inner class RemoteConfigTest {
        @Test
        fun `Workspace 를 가져오지 못하면 평가 없이 defaultValue 로 결정한다`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            every { workspaceFetcher.workspace(any()) } returns null

            // when
            val actual = sut.remoteConfig("42", user, ValueType.STRING, "default value")

            //then
            val decision = RemoteConfigDecision.of("default value", DecisionReason.SDK_NOT_READY)
            expectThat(actual) isEqualTo decision
            verify { evaluateProcessor wasNot Called }
        }

        @Test
        fun `parameterKey 에 해당하는 RemoteConfigParameter 가 없으면 평가 없이 defaultValue 로 결정한다`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            val workspace = Workspaces.evaluation()
            every { workspaceFetcher.workspace(any()) } returns workspace

            // when
            val actual = sut.remoteConfig("42", user, ValueType.STRING, "default value")

            //then
            val decision = RemoteConfigDecision.of("default value", DecisionReason.REMOTE_CONFIG_PARAMETER_NOT_FOUND)
            expectThat(actual) isEqualTo decision
            verify { evaluateProcessor wasNot Called }
        }

        @Test
        fun `평가 결과로 결정한다`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            val parameter = RemoteConfigs.remoteResult(
                key = "42",
                value = RemoteConfigs.value(id = 42, rawValue = "REMOTE"),
                reason = DecisionReason.DEFAULT_RULE
            )
            val workspace = Workspaces.evaluation(remoteConfigParameters = listOf(parameter))
            every { workspaceFetcher.workspace(any()) } returns workspace

            val response = RemoteConfigs.response(
                user = user,
                workspace = workspace,
                evaluation = parameter.toEvaluation()
            )
            every { evaluateProcessor.remoteConfig(any()) } returns response

            // when
            val actual = sut.remoteConfig("42", user, ValueType.STRING, "default value")

            // then
            expectThat(actual) isEqualTo RemoteConfigDecision.of("REMOTE", DecisionReason.DEFAULT_RULE)
        }
    }
}
