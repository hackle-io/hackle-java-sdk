package io.hackle.sdk.core.model

/**
 * @author Yong
 */
data class Experiment(
    val id: Long,
    val key: Long,
    val type: Type,
    val identifierType: String,
    val status: Status,
    val variations: List<Variation>,
    val userOverrides: Map<String, Long>,
    val segmentOverrides: List<TargetRule>,
    val targetAudiences: List<Target>,
    val targetRules: List<TargetRule>,
    val defaultRule: Action,
    private val winnerVariationId: Long?
) {

    val winnerVariation: Variation? get() = if (winnerVariationId != null) getVariationOrNull(winnerVariationId) else null

    internal fun getVariationOrNull(variationId: Long): Variation? {
        return variations.find { it.id == variationId }
    }

    internal fun getVariationOrNull(variationKey: String): Variation? {
        return variations.find { it.key == variationKey }
    }

    enum class Status {
        DRAFT, RUNNING, PAUSED, COMPLETED;

        companion object {

            private val STATUSES = mapOf(
                "READY" to DRAFT,
                "RUNNING" to RUNNING,
                "PAUSED" to PAUSED,
                "STOPPED" to COMPLETED
            )

            fun fromExecutionStatusOrNull(code: String): Status? {
                return STATUSES[code]
            }
        }
    }

    enum class Type {
        AB_TEST, FEATURE_FLAG
    }
}
