package io.hackle.sdk.core.evaluation.mode.remote

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResponse
import io.hackle.sdk.core.evaluation.service.experiment.mode.remote.ExperimentRemoteEvaluateRequest
import io.hackle.sdk.core.model.DefaultEntity
import io.hackle.sdk.core.model.ServiceType
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.Workspaces
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs

class RemoteEvaluatorTest {

    private val sut = TestRemoteEvaluator()

    @Test
    fun `entity 의 reference 를 workspace 에서 찾아 context 에 추가한 뒤 평가한다`() {
        // given
        val referenced = Experiments.remoteResult(id = 1)
        val experiment = Experiments.remoteResult(
            id = 2,
            references = listOf(DefaultEntity(referenced.serviceType, referenced.id))
        )
        val workspace = Workspaces.evaluation(experiments = listOf(referenced, experiment))
        val request = Experiments.remoteRequest(workspace = workspace, experiment = experiment)
        val context = Evaluators.context()

        // when
        val response = sut.evaluate(request, context)

        // then
        expectThat(context[referenced])
            .isNotNull()
            .get { entity } isSameInstanceAs referenced
        expectThat(response.references).hasSize(1)
        expectThat(response.evaluation.entity) isSameInstanceAs experiment
    }

    @Test
    fun `이미 context 에 추가된 reference 는 다시 추가하지 않는다`() {
        // given
        val referenced = Experiments.remoteResult(id = 1)
        val existingEvaluation = referenced.toEvaluation()
        val experiment = Experiments.remoteResult(
            id = 2,
            references = listOf(DefaultEntity(referenced.serviceType, referenced.id))
        )
        val workspace = Workspaces.evaluation(experiments = listOf(referenced, experiment))
        val request = Experiments.remoteRequest(workspace = workspace, experiment = experiment)
        val context = Evaluators.context()
        context.add(existingEvaluation)

        // when
        val response = sut.evaluate(request, context)

        // then
        expectThat(context[referenced]) isSameInstanceAs existingEvaluation
        expectThat(response.references).hasSize(1)
    }

    @Test
    fun `workspace 에서 reference 결과를 찾지 못하면 무시하고 평가한다`() {
        // given
        val experiment = Experiments.remoteResult(
            references = listOf(DefaultEntity(ServiceType.AB_TEST, 999))
        )
        val workspace = Workspaces.evaluation(experiments = listOf(experiment))
        val request = Experiments.remoteRequest(workspace = workspace, experiment = experiment)
        val context = Evaluators.context()

        // when
        val response = sut.evaluate(request, context)

        // then
        expectThat(response.references).hasSize(0)
        expectThat(response.evaluation.entity) isSameInstanceAs experiment
    }

    private class TestRemoteEvaluator :
        RemoteEvaluator<ExperimentRemoteEvaluateRequest, ExperimentEvaluateResponse>() {

        override fun supports(request: EvaluateRequest): Boolean {
            return true
        }

        override fun remoteEvaluate(
            request: ExperimentRemoteEvaluateRequest,
            context: Evaluator.Context,
        ): ExperimentEvaluateResponse {
            return ExperimentEvaluateResponse.of(request, context, request.entity)
        }

        override fun record(request: ExperimentRemoteEvaluateRequest, response: ExperimentEvaluateResponse) {
        }
    }
}
