package io.hackle.sdk.core.evaluation.service.inappmessage.layout

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.InAppMessages
import io.hackle.sdk.core.support.Workspaces
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class InAppMessageLayoutEvaluateResponseTest {

    @Nested
    inner class OfTest {

        @Test
        fun `request, context, result 로 생성한다`() {
            val workspace = Workspaces.evaluation()
            val inAppMessage = InAppMessages.layoutRemoteResult()
            val request = InAppMessages.layoutRemoteRequest(workspace = workspace, inAppMessage = inAppMessage)
            val result = InAppMessages.layoutResult()

            val response = InAppMessageLayoutEvaluateResponse.of(request, Evaluators.context(), result)

            expectThat(response) {
                get { user } isSameInstanceAs request.user
                get { this.workspace } isSameInstanceAs workspace
                get { evaluation.entity } isSameInstanceAs inAppMessage
                get { evaluation.result } isSameInstanceAs result
                get { references }.hasSize(0)
            }
        }

        @Test
        fun `experimentContext 가 없으면 experiment 는 null`() {
            val inAppMessage = InAppMessages.layoutRemoteResult(
                messageContext = InAppMessages.messageContext(experimentContext = null)
            )
            val request = InAppMessages.layoutRemoteRequest(inAppMessage = inAppMessage)

            val response =
                InAppMessageLayoutEvaluateResponse.of(request, Evaluators.context(), InAppMessages.layoutResult())

            expectThat(response.experiment).isNull()
        }

        @Test
        fun `experimentContext 의 실험 평가를 context 에서 찾아 experiment 에 담는다`() {
            val experiment = Experiments.remoteResult(id = 5, key = 42)
            val workspace = Workspaces.evaluation(experiments = listOf(experiment))
            val inAppMessage = InAppMessages.layoutRemoteResult(
                messageContext = InAppMessages.messageContext(
                    experimentContext = InAppMessages.experimentContext(key = 42)
                )
            )
            val request = InAppMessages.layoutRemoteRequest(workspace = workspace, inAppMessage = inAppMessage)
            val context = Evaluators.context()
            val experimentEvaluation = experiment.toEvaluation()
            context.add(experimentEvaluation)

            val response = InAppMessageLayoutEvaluateResponse.of(request, context, InAppMessages.layoutResult())

            expectThat(response.experiment) isSameInstanceAs experimentEvaluation
            expectThat(response.references).hasSize(1)
        }

        @Test
        fun `workspace 에서 실험을 찾지 못하면 experiment 는 null`() {
            val inAppMessage = InAppMessages.layoutRemoteResult(
                messageContext = InAppMessages.messageContext(
                    experimentContext = InAppMessages.experimentContext(key = 42)
                )
            )
            val request = InAppMessages.layoutRemoteRequest(
                workspace = Workspaces.evaluation(experiments = emptyList()),
                inAppMessage = inAppMessage
            )

            val response =
                InAppMessageLayoutEvaluateResponse.of(request, Evaluators.context(), InAppMessages.layoutResult())

            expectThat(response.experiment).isNull()
        }

        @Test
        fun `context 에 실험 평가가 없으면 experiment 는 null`() {
            val experiment = Experiments.remoteResult(id = 5, key = 42)
            val inAppMessage = InAppMessages.layoutRemoteResult(
                messageContext = InAppMessages.messageContext(
                    experimentContext = InAppMessages.experimentContext(key = 42)
                )
            )
            val request = InAppMessages.layoutRemoteRequest(
                workspace = Workspaces.evaluation(experiments = listOf(experiment)),
                inAppMessage = inAppMessage
            )

            val response =
                InAppMessageLayoutEvaluateResponse.of(request, Evaluators.context(), InAppMessages.layoutResult())

            expectThat(response.experiment).isNull()
        }
    }
}
