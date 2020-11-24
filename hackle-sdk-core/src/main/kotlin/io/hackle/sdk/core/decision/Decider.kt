package io.hackle.sdk.core.decision

import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Experiment

/**
 * @author Yong
 */
internal interface Decider {
    fun decide(experiment: Experiment, user: User): Decision
}
