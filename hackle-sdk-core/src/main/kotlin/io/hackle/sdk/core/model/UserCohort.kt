package io.hackle.sdk.core.model

data class UserCohort(
    val identifier: Identifier,
    val cohorts: List<Cohort>,
)
