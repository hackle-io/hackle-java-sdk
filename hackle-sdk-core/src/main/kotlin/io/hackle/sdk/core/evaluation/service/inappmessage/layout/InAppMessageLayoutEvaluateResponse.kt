package io.hackle.sdk.core.evaluation.service.inappmessage.layout

import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluation
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

class InAppMessageLayoutEvaluateResponse(
    override val user: HackleUser,
    override val workspace: Workspace,
    override val evaluation: InAppMessageLayoutEvaluation,
    override val references: List<Evaluation>,
    val experiment: ExperimentEvaluation?,
) : EvaluateResponse {

    companion object {
        fun of(
            request: InAppMessageLayoutEvaluateRequest,
            context: Evaluator.Context,
            result: InAppMessageLayoutEvaluateResult,
        ): InAppMessageLayoutEvaluateResponse {
            val experimentEvaluation = request.entity.experimentContext?.key
                ?.let { request.workspace.getExperimentOrNull(it) }
                ?.let { context[it] as? ExperimentEvaluation }
            return InAppMessageLayoutEvaluateResponse(
                user = request.user,
                workspace = request.workspace,
                evaluation = InAppMessageLayoutEvaluation(entity = request.entity, result = result),
                references = context.references,
                experiment = experimentEvaluation
            )
        }
    }
}
