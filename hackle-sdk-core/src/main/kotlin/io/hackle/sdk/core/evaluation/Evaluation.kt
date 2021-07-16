package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.model.Variation

internal sealed class Evaluation {

    abstract val reason: DecisionReason
    abstract val variationId: Long?
    abstract val variationKey: String?

    class Identified(
        override val reason: DecisionReason,
        val variation: Variation
    ) : Evaluation() {
        override val variationId: Long get() = variation.id
        override val variationKey: String get() = variation.key
    }

    class Forced(
        override val reason: DecisionReason,
        override val variationKey: String
    ) : Evaluation() {
        override val variationId: Long? get() = null
    }

    class Default(
        override val reason: DecisionReason,
        override val variationKey: String
    ) : Evaluation() {
        override val variationId: Long? get() = null
    }

    class None(override val reason: DecisionReason) : Evaluation() {
        override val variationId: Long? get() = null
        override val variationKey: String? get() = null
    }
}