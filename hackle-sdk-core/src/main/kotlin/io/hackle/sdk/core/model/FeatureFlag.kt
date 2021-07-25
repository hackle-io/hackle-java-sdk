package io.hackle.sdk.core.model

import io.hackle.sdk.common.User

typealias FeatureFlag = Experiment
//class FeatureFlag(
//    val id: Long,
//    val key: Long,
//    val bucket: Bucket,
//    val variations: Map<Long, Variation>,
//    val overrides: Map<String, Long>,
//) {
//
//
//    fun getVariationOrNull(variationId: Long): Variation? {
//        return variations[variationId]
//    }
//
//    fun getOverriddenVariationOrNull(user: User): Variation? {
//        val overriddenVariationId = overrides[user.id] ?: return null
//        val overriddenVariation = getVariationOrNull(overriddenVariationId)
//        return requireNotNull(overriddenVariation) { "experiment[$id] variation[$overriddenVariationId]" }
//    }
//}
