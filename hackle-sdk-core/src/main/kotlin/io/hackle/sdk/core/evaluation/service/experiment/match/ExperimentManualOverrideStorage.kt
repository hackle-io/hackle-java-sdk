package io.hackle.sdk.core.evaluation.service.experiment.match

import io.hackle.sdk.core.model.Variation
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.config.entity.ExperimentConfig

interface ExperimentManualOverrideStorage {
    operator fun get(experiment: ExperimentConfig, user: HackleUser): Variation?
}

class DelegatingExperimentManualOverrideStorage(
    private val storages: List<ExperimentManualOverrideStorage>,
) : ExperimentManualOverrideStorage {
    override fun get(experiment: ExperimentConfig, user: HackleUser): Variation? {
        return storages.firstNotNullOfOrNull { it[experiment, user] }
    }
}


object NoopExperimentManualOverrideStorage : ExperimentManualOverrideStorage {
    override fun get(experiment: ExperimentConfig, user: HackleUser): Variation? {
        return null
    }
}
