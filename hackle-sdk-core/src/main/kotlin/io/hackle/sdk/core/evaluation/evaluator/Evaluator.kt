package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace


/**
 * @author Yong
 */
internal interface Evaluator {

    /**
     * @param request
     * @param context Mutable context that Evaluator may need during an evaluation.
     *
     * @return results of an evaluation
     */
    fun evaluate(request: Request, context: Context): Evaluation


    enum class Type {
        EXPERIMENT,
        REMOTE_CONFIG
    }

    data class Key(
        val type: Type,
        val id: Long
    )

    interface Request {
        val key: Key
        val workspace: Workspace
        val user: HackleUser
    }

    interface Evaluation {
        val reason: DecisionReason
        val targetEvaluations: List<Evaluation>
    }

    /**
     * Mutable context that [Evaluator] may need during an evaluation.
     */
    interface Context {
        val stack: List<Request>
        val evaluations: List<Evaluation>

        operator fun contains(request: Request): Boolean
        fun add(request: Request)
        fun remove(request: Request)

        operator fun get(experiment: Experiment): Evaluation?
        fun add(evaluation: Evaluation)
    }
}
