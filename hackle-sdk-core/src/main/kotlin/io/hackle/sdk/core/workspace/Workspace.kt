package io.hackle.sdk.core.workspace

import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.FeatureFlag

/**
 * @author Yong
 */
interface Workspace {

    fun getExperimentOrNull(experimentKey: Long): Experiment?

    fun getFeatureFlagOrNull(featureFlagKey: Long): FeatureFlag?

    fun getEventTypeOrNull(eventTypeKey: String): EventType?
}
