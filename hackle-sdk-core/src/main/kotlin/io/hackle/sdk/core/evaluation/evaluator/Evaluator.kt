package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace


/**
 * @author Yong
 */
interface Evaluator {

    fun evaluate(request: Request, context: Context): Evaluation

    enum class Type {
        EXPERIMENT,
        REMOTE_CONFIG,
        IN_APP_MESSAGE
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

    interface EventRequest : Request {
        val event: UserEvent.Track
    }

    interface Evaluation {
        val reason: DecisionReason
        val targetEvaluations: List<Evaluation>
    }

    interface Context {
        val stack: List<Request>
        val targetEvaluations: List<Evaluation>

        operator fun contains(request: Request): Boolean
        fun add(request: Request)
        fun remove(request: Request)

        operator fun get(experiment: Experiment): Evaluation?
        fun add(evaluation: Evaluation)
    }
}
