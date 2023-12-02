package io.hackle.sdk.internal.client

import io.hackle.sdk.common.HackleRemoteConfig
import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason.EXCEPTION
import io.hackle.sdk.common.decision.DecisionReason.INVALID_INPUT
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.HackleCore
import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.model.ValueType.*
import io.hackle.sdk.internal.monitoring.metrics.DecisionMetrics
import io.hackle.sdk.internal.user.HackleUserResolver

internal class HackleRemoteConfigImpl(
    private val user: User,
    private val core: HackleCore,
    private val userResolver: HackleUserResolver,
) : HackleRemoteConfig {

    override fun getString(key: String, defaultValue: String): String {
        return get(user, key, STRING, defaultValue).value
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return get<Number>(user, key, NUMBER, defaultValue).value.toInt()
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return get<Number>(user, key, NUMBER, defaultValue).value.toLong()
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return get<Number>(user, key, NUMBER, defaultValue).value.toDouble()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return get(user, key, BOOLEAN, defaultValue).value
    }

    private fun <T : Any> get(
        user: User,
        key: String,
        requiredType: ValueType,
        defaultValue: T
    ): RemoteConfigDecision<T> {
        val sample = Timer.start()
        return try {
            val hackleUser = userResolver.resolveOrNull(user)
            if (hackleUser == null) {
                RemoteConfigDecision.of(defaultValue, INVALID_INPUT)
            } else {
                core.remoteConfig(key, hackleUser, requiredType, defaultValue)
            }
        } catch (e: Exception) {
            log.error { "Unexpected exception while deciding remote config parameter[$key]. Returning default value." }
            RemoteConfigDecision.of(defaultValue, EXCEPTION)
        }.also {
            DecisionMetrics.remoteConfig(sample, key, it)
        }
    }

    companion object {
        private val log = Logger<HackleRemoteConfigImpl>()
    }
}
