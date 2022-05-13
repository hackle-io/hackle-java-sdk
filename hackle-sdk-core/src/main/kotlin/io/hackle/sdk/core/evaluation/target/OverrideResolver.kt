package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.HackleUser
import io.hackle.sdk.core.model.Variation
import io.hackle.sdk.core.workspace.Workspace

internal class OverrideResolver(
    private val targetMatcher: TargetMatcher,
    private val actionResolver: ActionResolver
) {

    fun resolveOrNull(workspace: Workspace, experiment: Experiment, user: HackleUser): Variation? {
        return resolveUserOverride(experiment, user) ?: resolveSegmentOverride(workspace, experiment, user)
    }

    private fun resolveUserOverride(experiment: Experiment, user: HackleUser): Variation? {
        val identifier = user.identifiers[experiment.identifierType] ?: return null
        val overriddenVariationId = experiment.userOverrides[identifier] ?: return null
        return experiment.getVariationOrNull(overriddenVariationId)
    }

    private fun resolveSegmentOverride(workspace: Workspace, experiment: Experiment, user: HackleUser): Variation? {
        val overriddenRule =
            experiment.segmentOverrides.find { targetMatcher.matches(it.target, workspace, user) } ?: return null

        return actionResolver.resolveOrNull(overriddenRule.action, workspace, experiment, user)
    }
}
