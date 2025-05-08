package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.model.InAppMessage

interface InAppMessageImpressionStorage {
    fun get(inAppMessage: InAppMessage): List<InAppMessageImpression>
    fun set(inAppMessage: InAppMessage, impressions: List<InAppMessageImpression>)
}

data class InAppMessageImpression(
    val identifiers: Map<String, String>,
    val timestamp: Long
)

internal object NoopInAppMessageImpressionStorage : InAppMessageImpressionStorage {
    override fun get(inAppMessage: InAppMessage): List<InAppMessageImpression> {
        return emptyList()
    }

    override fun set(inAppMessage: InAppMessage, impressions: List<InAppMessageImpression>) {
        // Noop
    }
}
