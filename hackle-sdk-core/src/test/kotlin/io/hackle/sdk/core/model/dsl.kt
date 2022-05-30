package io.hackle.sdk.core.model

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.core.model.Target.Key.Type.USER_ID
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.model.Target.Match.Type.MATCH
import io.hackle.sdk.core.model.Target.Match.ValueType.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlin.reflect.full.isSubclassOf

typealias VariationKey = io.hackle.sdk.common.Variation

fun interface BucketRegistry {
    fun register(bucket: Bucket)

    object None : BucketRegistry {
        override fun register(bucket: Bucket) {}
    }
}

fun experiment(
    id: Long = IdentifierGenerator.generate("experiment"),
    key: Long = IdentifierGenerator.generate("experimentKey"),
    identifierType: String = "\$id",
    type: Experiment.Type,
    status: Experiment.Status,
    bucketRegistry: BucketRegistry = BucketRegistry.None,
    init: ExperimentDsl.() -> Unit = { variations(A, B) }
): Experiment {
    return ExperimentDsl(id, key, type, identifierType, status, bucketRegistry).apply(init).build()
}

class ExperimentDsl(
    private val id: Long,
    private val key: Long,
    private val type: Experiment.Type,
    private val identifierType: String,
    private val status: Experiment.Status,
    private val bucketRegistry: BucketRegistry,
) {

    private lateinit var variations: List<Variation>
    private var winnerVariationId: Long? = null
    private val overrides = mutableMapOf<String, Long>()
    private val segmentOverrides = mutableListOf<TargetRule>()
    private val targetAudiences = mutableListOf<Target>()
    private val targetRules = mutableListOf<TargetRule>()
    private var defaultRule: Action? = null

    // Variation
    fun variations(init: VariationDsl.() -> Unit) {
        variations = VariationDsl().apply(init).build()
    }

    fun variations(vararg variations: VariationKey) {
        variations {
            for (variation in variations) {
                +variation
            }
        }
    }

    fun winner(key: VariationKey) {
        val variation = variations.first { it.key == key.name }
        winnerVariationId = variation.id
    }

    // Override
    fun overrides(init: OverrideDsl.() -> Unit) {
        val (userOverrides, segmentOverrides) = OverrideDsl(variations).apply(init).build()
        this.overrides.putAll(userOverrides)
        this.segmentOverrides.addAll(segmentOverrides)
    }

    // Audiences
    fun audiences(init: AudiencesDsl.() -> Unit) {
        targetAudiences.addAll(AudiencesDsl().apply(init).build())
    }

    // TargetRules
    fun targetRules(init: TargetRulesDsl.() -> Unit) {
        targetRules.addAll(TargetRulesDsl(variations, bucketRegistry).apply(init).build())
    }

    // DefaultRule
    fun defaultRule(init: ActionDsl.() -> Unit) {
        val action = ActionDsl(variations, bucketRegistry).apply(init).build()
        defaultRule = action
    }

    fun build(): Experiment {
        val defaultRule = this.defaultRule ?: ActionDsl(variations, bucketRegistry).apply { bucket() }.build()
        return Experiment(
            id,
            key,
            type,
            identifierType,
            status,
            variations,
            overrides,
            segmentOverrides,
            targetAudiences,
            targetRules,
            defaultRule,
            winnerVariationId
        )
    }
}

class TargetRulesDsl(
    private val variations: List<Variation>,
    private val bucketRegistry: BucketRegistry,
) {
    private val targetRules = mutableListOf<TargetRule>()

    fun targetRule(init: TargetRuleDsl.() -> Unit) {
        targetRules += TargetRuleDsl(variations, bucketRegistry).apply(init).build()
    }

    fun build(): List<TargetRule> {
        return targetRules
    }
}

class TargetRuleDsl(
    private val variations: List<Variation>,
    private val bucketRegistry: BucketRegistry,
) {

    private lateinit var target: Target
    private lateinit var action: Action
    fun target(init: TargetDsl.() -> Unit) {
        target = TargetDsl().apply(init).build()
    }

    fun action(init: ActionDsl.() -> Unit) {
        action = ActionDsl(variations, bucketRegistry).apply(init).build()
    }

    fun build(): TargetRule {
        return TargetRule(target, action)
    }
}


class ActionDsl(
    private val variations: List<Variation>,
    private val bucketRegistry: BucketRegistry
) {

    private lateinit var action: Action

    fun variation(key: VariationKey) {
        val variation = variations.first { it.key == key.name }
        action = Action.Variation(variation.id)
    }

    fun bucket(
        id: Long = IdentifierGenerator.generate("bucket"),
        seed: Int = Random.nextInt(Int.MAX_VALUE),
        slotSize: Int = 10000,
        init: BucketDsl.() -> Unit = {}
    ) {

        val bucket = BucketDsl(id, seed, slotSize, variations, bucketRegistry).apply(init).build()
        action = Action.Bucket(bucket.id)
    }

    fun build(): Action {
        return action
    }
}


class AudiencesDsl {

    private val targets = mutableListOf<Target>()

    fun target(init: TargetDsl.() -> Unit) {
        targets += TargetDsl().apply(init).build()
    }

    fun build(): List<Target> {
        return targets
    }
}

fun target(init: TargetDsl.() -> Unit): Target {
    return TargetDsl().apply(init).build()
}

fun condition(init: TargetDsl.ConditionDsl.() -> Unit): Target.Condition {
    return TargetDsl.ConditionDsl().apply(init).build()
}

class TargetDsl {

    private val conditions = mutableListOf<Target.Condition>()

    fun condition(init: ConditionDsl.() -> Unit) {
        conditions += ConditionDsl().apply(init).build()
    }

    fun build(): Target {
        return Target(conditions)
    }

    class ConditionDsl {

        private lateinit var key: Target.Key
        private lateinit var match: Target.Match

        fun key(type: Target.Key.Type, name: String) {
            key = Target.Key(type, name)
        }

        operator fun Target.Key.Type.invoke(name: String) {
            key = Target.Key(this, name)
        }

        fun match(match: Target.Match) {
            this.match = match
        }

        fun match(
            type: Target.Match.Type,
            operator: Target.Match.Operator,
            valueType: Target.Match.ValueType,
            vararg values: Any
        ) {
            match = Target.Match(type, operator, valueType, values.toList())
        }


        internal inline operator fun <reified T : Any> Target.Match.Operator.invoke(vararg values: T) {
            val match = when {
                T::class.isSubclassOf(String::class) -> Target.Match(MATCH, this, STRING, values.toList())
                T::class.isSubclassOf(Number::class) -> Target.Match(MATCH, this, NUMBER, values.toList())
                T::class.isSubclassOf(Boolean::class) -> Target.Match(MATCH, this, BOOLEAN, values.toList())
                T::class.isSubclassOf(Version::class) -> Target.Match(
                    MATCH,
                    this,
                    VERSION,
                    values.map { (it as Version).plainString })
                else -> throw IllegalArgumentException("Unsupported type [${T::class.java.simpleName}]")
            }
            match(match)
        }

        fun build(): Target.Condition {
            return Target.Condition(key, match)
        }
    }
}

class VariationDsl {

    private val variations = mutableListOf<Variation>()

    operator fun VariationKey.invoke(
        id: Long = IdentifierGenerator.generate("variation"),
        isDropped: Boolean = false
    ) {
        variations += Variation(id, this.name, isDropped)
    }

    operator fun VariationKey.unaryPlus() {
        this()
    }

    fun build(): List<Variation> {
        return variations
    }
}


class OverrideDsl(private val variations: List<Variation>) {

    private val overrides = mutableMapOf<String, Long>()
    private val segmentOverrides = mutableListOf<TargetRule>()

    operator fun VariationKey.invoke(vararg userIds: String) {
        val variation = variations.first { it.key == this.name }
        for (userId in userIds) {
            overrides[userId] = variation.id
        }
    }

    operator fun VariationKey.invoke(init: OverrideVariationDsl.() -> Unit) {
        val (overrides, segmentOverride) =
            OverrideVariationDsl(variations.first { it.key == this.name }).apply(init).build()
        this@OverrideDsl.overrides.putAll(overrides)
        if (segmentOverride != null) {
            this@OverrideDsl.segmentOverrides.add(segmentOverride)
        }
    }

    fun build(): Pair<Map<String, Long>, List<TargetRule>> {
        return overrides to segmentOverrides
    }

    class OverrideVariationDsl(private val variation: Variation) {

        private val overrides = mutableMapOf<String, Long>()
        private var segmentOverride: TargetRule? = null

        fun user(vararg userIds: String) {
            for (userId in userIds) {
                overrides[userId] = variation.id
            }
        }

        fun segment(vararg segmentKeys: String) {
            val target = target {
                condition {
                    USER_ID("USER_ID")
                    IN(*segmentKeys)
                }
            }
            val action = Action.Variation(variation.id)
            segmentOverride = TargetRule(target, action)
        }

        fun build(): Pair<Map<String, Long>, TargetRule?> {
            return overrides to segmentOverride
        }
    }
}


class BucketDsl(
    private val id: Long,
    private val seed: Int,
    private val slotSize: Int,
    private val variations: List<Variation>,
    private val bucketRegistry: BucketRegistry,
) {
    private val slots = mutableListOf<Slot>()

    operator fun VariationKey.invoke(range: IntRange) {
        val variation = variations.first { it.key == this.name }
        slots += Slot(range.first, range.last, variation.id)
    }

    fun build(): Bucket {
        return Bucket(id, seed, slotSize, slots).also {
            bucketRegistry.register(it)
        }
    }
}

class SegmentDsl(
    private val id: Long,
    private val key: String,
    private val type: Segment.Type,
) {

    private val targets = mutableListOf<Target>()

    fun target(init: TargetDsl.() -> Unit) {
        targets += TargetDsl().apply(init).build()
    }

    fun build(): Segment {
        return Segment(id, key, type, targets)
    }
}

object IdentifierGenerator {
    private val store = ConcurrentHashMap<String, Long>()
    fun generate(type: String): Long {
        return requireNotNull(store.compute(type) { _, id -> if (id == null) 1 else id + 1 })
    }
}