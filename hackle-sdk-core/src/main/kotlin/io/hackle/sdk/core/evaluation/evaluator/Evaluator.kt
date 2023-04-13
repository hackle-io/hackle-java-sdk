package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace


/**
 * @author Yong
 */
internal interface Evaluator {

    fun evaluate(request: Request, context: Context): Evaluation

    interface Request {
        val workspace: Workspace
        val user: HackleUser
    }

    interface Evaluation {
        val reason: DecisionReason
        val context: Context
    }

    interface Context {
        object Empty : Context
        companion object {
            fun create(): Context = Empty
        }
    }
}
