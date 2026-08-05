package io.hackle.sdk.core.workspace.config.entity

import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Target

class ExperimentConfig(
    override val id: Long,
    override val key: Long,
    override val version: Int,
    override val status: Experiment.Status,
    override val order: Long,
    override val type: Experiment.Type,
    override val executionVersion: Int,
    val name: String?,
    val identifierType: String,
    val variations: List<Variation>,
    val userOverrides: Map<String, Long>,
    val segmentOverrides: List<TargetRule>,
    val targetAudiences: List<Target>,
    val targetRules: List<TargetRule>,
    val defaultRule: Action,
    val containerId: Long?,
    private val winnerVariationId: Long?,
) : AbstractExperiment(), ConfigEntity {

    val controlVariation: Variation get() = requireNotNull(getVariationOrNull(io.hackle.sdk.common.Variation.CONTROL.name)) { "ControlVariation[$id]" }
    val winnerVariation: Variation? get() = if (winnerVariationId != null) getVariationOrNull(winnerVariationId) else null

    fun getVariationOrNull(variationId: Long): Variation? {
        return variations.find { it.id == variationId }
    }

    fun getVariationOrNull(variationKey: String): Variation? {
        return variations.find { it.key == variationKey }
    }

    override fun toString(): String {
        return "Experiment(id=$id, key=$key, version=$version, status=$status)"
    }
}
