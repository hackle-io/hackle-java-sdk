package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target

internal fun Target.Match.Type.matches(isMatched: Boolean): Boolean {
    return when (this) {
        Target.Match.Type.MATCH -> isMatched
        Target.Match.Type.NOT_MATCH -> !isMatched
    }
}
