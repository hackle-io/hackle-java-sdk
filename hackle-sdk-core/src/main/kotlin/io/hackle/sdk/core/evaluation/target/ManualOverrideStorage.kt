package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Variation
import io.hackle.sdk.core.user.HackleUser

interface ManualOverrideStorage {
    operator fun get(experiment: Experiment, user: HackleUser): Variation?
}

internal class DelegatingManualOverrideStorage(
    private val storages: List<ManualOverrideStorage>
) : ManualOverrideStorage {
    override fun get(experiment: Experiment, user: HackleUser): Variation? {
        return storages.asSequence()
            .mapNotNull { it[experiment, user] }
            .firstOrNull()
    }
}
