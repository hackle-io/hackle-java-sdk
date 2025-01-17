package io.hackle.sdk.core.model

import io.hackle.sdk.common.HackleExperiment

/**
 * @author Yong
 */
data class Experiment(
    val id: Long,
    override val key: Long,
    val name: String?,
    val type: Type,
    val identifierType: String,
    val status: Status,
    override val version: Int,
    val executionVersion: Int,
    val variations: List<Variation>,
    val userOverrides: Map<String, Long>,
    val segmentOverrides: List<TargetRule>,
    val targetAudiences: List<Target>,
    val targetRules: List<TargetRule>,
    val defaultRule: Action,
    val containerId: Long?,
    private val winnerVariationId: Long?
) : HackleExperiment {

    val winnerVariation: Variation? get() = if (winnerVariationId != null) getVariationOrNull(winnerVariationId) else null

    fun getVariationOrNull(variationId: Long): Variation? {
        return variations.find { it.id == variationId }
    }

    fun getVariationOrNull(variationKey: String): Variation? {
        return variations.find { it.key == variationKey }
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> return true
            other !is Experiment -> return false
            else -> this.id == other.id
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Experiment(id=$id, key=$key, version=$version)"
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
