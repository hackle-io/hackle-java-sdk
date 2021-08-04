package io.hackle.sdk.core.workspace

import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.model.Experiment

/**
 * @author Yong
 */
interface Workspace {

    fun getExperimentOrNull(experimentKey: Long): Experiment?

    fun getFeatureFlagOrNull(featureKey: Long): Experiment?

    fun getEventTypeOrNull(eventTypeKey: String): EventType?
}
