package io.hackle.sdk.core.evaluation

import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.container.ContainerResolver
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.match.ConditionMatcherFactory
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.target.*
import io.hackle.sdk.core.internal.time.Clock

class EvaluationContext internal constructor() {

    private val instances = mutableListOf<Any>()

    fun <T : Any> register(instance: T): T {
        instances.add(instance)
        return instance
    }

    fun <T : Any> getOrNull(type: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return instances.find { type.isInstance(it) } as? T
    }

    operator fun <T : Any> get(type: Class<T>): T {
        return requireNotNull(getOrNull(type)) { "Instance not registered [${type.simpleName}]" }
    }

    internal fun initialize(evaluator: Evaluator, manualOverrideStorage: ManualOverrideStorage, clock: Clock) {
        register(evaluator)
        register(manualOverrideStorage)
        register(Bucketer())
        register(clock)
        register(ConditionMatcherFactory(get(), get()))
        register(TargetMatcher(get()))
        register(ActionResolver(get()))
        register(OverrideResolver(get(), get(), get()))
        register(ContainerResolver(get()))
        register(ExperimentTargetDeterminer(get()))
        register(ExperimentTargetRuleDeterminer(get()))
        register(RemoteConfigParameterTargetRuleDeterminer.Matcher(get(), get()))
        register(RemoteConfigParameterTargetRuleDeterminer(get()))
        register(InAppMessageResolver(get()))
        register(InAppMessageUserOverrideMatcher())
        register(InAppMessageTargetMatcher(get()))
        // server sdk: NoopInAppMessageHiddenStorage
        // android sdk: AndroidInAppMessageHiddenStorage
        //  hackle-android-sdk 에서 초기화 할 때 EvaluationContext.GLOBAL에 등록함
        register(InAppMessageHiddenMatcher(getOrNull() ?: NoopInAppMessageHiddenStorage))
    }

    companion object {
        val GLOBAL = EvaluationContext()
    }
}

inline fun <reified T : Any> EvaluationContext.get(): T {
    return this[T::class.java]
}

inline fun <reified T : Any> EvaluationContext.getOrNull(): T? {
    return getOrNull(T::class.java)
}
