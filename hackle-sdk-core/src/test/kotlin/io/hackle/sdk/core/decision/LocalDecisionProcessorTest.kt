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
import io.hackle.sdk.core.workspace.config.WorkspaceConfigFetcher
import io.hackle.sdk.core.workspace.config.entity.ExperimentConfig
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
import strikt.assertions.isNull

@ExtendWith(MockKExtension::class)
class LocalDecisionProcessorTest {

    @MockK
    private lateinit var workspaceFetcher: WorkspaceConfigFetcher

    @MockK
    private lateinit var evaluateProcessor: EvaluateProcessor

    @InjectMockKs
    private lateinit var sut: LocalDecisionProcessor

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
            val workspace = Workspaces.config()
            every { workspaceFetcher.workspace(any()) } returns workspace

            // when
            val actual = sut.experiment(42, user)

            //then
            expectThat(actual) {
                get { reason } isEqualTo DecisionReason.EXPERIMENT_NOT_FOUND
                get { variation } isEqualTo VariationKey.CONTROL
                get { experiment }.isNull()
            }
            verify { evaluateProcessor wasNot Called }
        }

        @Test
        fun `평가 결과로 결정한다`() {
            // given
            val user = HackleUser.builder().identifier(IdentifierType.DEVICE, "device").build()
            val parameterConfiguration = parameterConfiguration(parameters = mapOf("hello" to "SDK"))
            val variation = Experiments.variation(key = "B", parameterConfiguration = parameterConfiguration)
            val experiment = Experiments.config(key = 42, variations = listOf(variation))
            val workspace = Workspaces.config(
                experiments = listOf(experiment)
            )
            every { workspaceFetcher.workspace(any()) } returns workspace


            val response = Experiments.response(
                user = user,
                workspace = workspace,
                evaluation = Experiments.evaluation(
                    entity = experiment,
                    result = Experiments.result(DecisionReason.TRAFFIC_ALLOCATED, variation)
                )
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
            val workspace = Workspaces.config(experiments = experiments)

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
        ): ExperimentConfig {
            val experiment = Experiments.config(id = id, key = key, variations = listOf(variation))
            val evaluation = Experiments.evaluation(
                entity = experiment,
                result = Experiments.result(reason, variation)
            )
            every { evaluateProcessor.experiment(any()) } answers {
                val request = firstArg<ExperimentEvaluateRequest>()
                Experiments.response(
                    user = request.user,
                    workspace = request.workspace,
                    evaluation = evaluation
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
            val workspace = Workspaces.config()
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
            val featureFlag = Experiments.config(
                key = 42,
                type = Experiment.Type.FEATURE_FLAG,
                variations = listOf(variation)
            )
            val workspace = Workspaces.config(
                featureFlags = listOf(featureFlag)
            )
            every { workspaceFetcher.workspace(any()) } returns workspace

            val response = Experiments.response(
                user = user,
                workspace = workspace,
                evaluation = Experiments.evaluation(
                    entity = featureFlag,
                    result = Experiments.result(DecisionReason.DEFAULT_RULE, variation)
                )
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
            val featureFlag = Experiments.config(
                key = 42,
                type = Experiment.Type.FEATURE_FLAG,
                variations = listOf(variation)
            )
            val workspace = Workspaces.config(
                featureFlags = listOf(featureFlag)
            )
            every { workspaceFetcher.workspace(any()) } returns workspace

            val response = Experiments.response(
                user = user,
                workspace = workspace,
                evaluation = Experiments.evaluation(
                    entity = featureFlag,
                    result = Experiments.result(DecisionReason.DEFAULT_RULE, variation)
                )
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
            val workspace = Workspaces.config(featureFlags = featureFlags)

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
        ): ExperimentConfig {
            val experiment = Experiments.config(
                id = id,
                key = key,
                type = Experiment.Type.FEATURE_FLAG,
                variations = listOf(variation)
            )
            val evaluation = Experiments.evaluation(
                entity = experiment,
                result = Experiments.result(reason, variation)
            )
            every { evaluateProcessor.experiment(any()) } answers {
                val request = firstArg<ExperimentEvaluateRequest>()
                Experiments.response(
                    user = request.user,
                    workspace = request.workspace,
                    evaluation = evaluation
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
            val workspace = Workspaces.config()
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
            val parameter = RemoteConfigs.config(key = "42")
            val workspace = Workspaces.config(remoteConfigParameters = listOf(parameter))
            every { workspaceFetcher.workspace(any()) } returns workspace


            val response = RemoteConfigs.response(
                user = user,
                workspace = workspace,
                evaluation = RemoteConfigs.evaluation(
                    parameter = parameter,
                    result = RemoteConfigs.result(
                        reason = DecisionReason.DEFAULT_RULE,
                        value = RemoteConfigs.value(id = 42, rawValue = "REMOTE")
                    )
                )
            )

            every { evaluateProcessor.remoteConfig(any()) } returns response

            // when
            val actual = sut.remoteConfig("42", user, ValueType.STRING, "default value")

            // then
            expectThat(actual) isEqualTo RemoteConfigDecision.of("REMOTE", DecisionReason.DEFAULT_RULE)
        }
    }
}
