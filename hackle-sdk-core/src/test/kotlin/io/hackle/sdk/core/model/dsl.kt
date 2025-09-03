package io.hackle.sdk.core.model

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility.InAppMessageEligibilityEvaluation
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility.InAppMessageEligibilityRequest
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout.InAppMessageLayoutEvaluation
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout.InAppMessageLayoutRequest
import io.hackle.sdk.core.model.Target.Key.Type.USER_ID
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.model.Target.Match.Type.MATCH
import io.hackle.sdk.core.model.ValueType.*
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.workspace
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

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
    version: Int = 1,
    executionVersion: Int = 1,
    type: Experiment.Type = Experiment.Type.AB_TEST,
    status: Experiment.Status = Experiment.Status.RUNNING,
    containerId: Long? = null,
    bucketRegistry: BucketRegistry = BucketRegistry.None,
    init: ExperimentDsl.() -> Unit = { variations(A, B) },
): Experiment {
    return ExperimentDsl(id, key, type, identifierType, status, version, executionVersion, containerId, bucketRegistry)
        .apply(init)
        .build()
}

class ExperimentDsl(
    private val id: Long,
    private val key: Long,
    private val type: Experiment.Type,
    private val identifierType: String,
    private val status: Experiment.Status,
    private val version: Int,
    private val executionVersion: Int,
    private val containerId: Long?,
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
            null,
            type,
            identifierType,
            status,
            version,
            executionVersion,
            variations,
            overrides,
            segmentOverrides,
            targetAudiences,
            targetRules,
            defaultRule,
            containerId,
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
    private val bucketRegistry: BucketRegistry,
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
        init: BucketDsl.() -> Unit = {},
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
            valueType: ValueType,
            vararg values: Any,
        ) {
            match(type, operator, valueType, values.toList())
        }

        fun match(
            type: Target.Match.Type,
            operator: Target.Match.Operator,
            valueType: ValueType,
            values: List<Any>,
        ) {
            match = Target.Match(type, operator, valueType, values)
        }

        internal inline operator fun <reified T : Any> Target.Match.Operator.invoke(vararg values: T) {

            val match = when {
                String::class.java.isAssignableFrom(T::class.java) -> Target.Match(MATCH, this, STRING, values.toList())
                Number::class.java.isAssignableFrom(T::class.java) -> Target.Match(MATCH, this, NUMBER, values.toList())
                Boolean::class.java.isAssignableFrom(T::class.java) -> Target.Match(
                    MATCH,
                    this,
                    BOOLEAN,
                    values.toList()
                )

                Version::class.java.isAssignableFrom(T::class.java) -> Target.Match(
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
        isDropped: Boolean = false,
        configId: Long? = null,
    ) {
        variations += Variation(id, this.name, isDropped, configId)
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


internal object InAppMessages {

    fun create(
        id: Long = 1,
        key: Long = 1,
        status: InAppMessage.Status = InAppMessage.Status.ACTIVE,
        period: InAppMessage.Period = InAppMessage.Period.Always,
        eventTrigger: InAppMessage.EventTrigger = eventTrigger(),
        evaluateContext: InAppMessage.EvaluateContext = InAppMessage.EvaluateContext(false),
        targetContext: InAppMessage.TargetContext = targetContext(),
        messageContext: InAppMessage.MessageContext = messageContext(),
    ): InAppMessage {
        return InAppMessage(
            id = id,
            key = key,
            status = status,
            period = period,
            eventTrigger = eventTrigger,
            evaluateContext = evaluateContext,
            targetContext = targetContext,
            messageContext = messageContext
        )
    }

    fun eventTrigger(
        rules: List<InAppMessage.EventTrigger.Rule> = listOf(InAppMessage.EventTrigger.Rule("test", emptyList())),
        frequencyCap: InAppMessage.EventTrigger.FrequencyCap? = null,
        delay: InAppMessage.Delay = InAppMessage.Delay(InAppMessage.Delay.Type.IMMEDIATE, null),
    ): InAppMessage.EventTrigger {
        return InAppMessage.EventTrigger(rules = rules, frequencyCap = frequencyCap, delay = delay)
    }

    fun frequencyCap(
        identifierCaps: List<InAppMessage.EventTrigger.IdentifierCap> = emptyList(),
        durationCap: InAppMessage.EventTrigger.DurationCap? = null,
    ): InAppMessage.EventTrigger.FrequencyCap {
        return InAppMessage.EventTrigger.FrequencyCap(identifierCaps, durationCap)
    }

    fun identifierCap(
        identifierType: String = "\$id",
        count: Int = 1,
    ): InAppMessage.EventTrigger.IdentifierCap {
        return InAppMessage.EventTrigger.IdentifierCap(identifierType, count)
    }

    fun durationCap(
        duration: Long = 60,
        count: Int = 1,
    ): InAppMessage.EventTrigger.DurationCap {
        return InAppMessage.EventTrigger.DurationCap(duration, count)
    }

    fun targetContext(
        targets: List<Target> = emptyList(),
        overrides: List<InAppMessage.UserOverride> = emptyList(),
    ): InAppMessage.TargetContext {
        return InAppMessage.TargetContext(targets, overrides)
    }

    fun messageContext(
        defaultLang: String = "ko",
        experimentContext: InAppMessage.ExperimentContext? = null,
        platformTypes: List<InAppMessage.PlatformType> = listOf(InAppMessage.PlatformType.ANDROID),
        orientations: List<InAppMessage.Orientation> = listOf(InAppMessage.Orientation.VERTICAL),
        messages: List<InAppMessage.Message> = listOf(message()),
    ): InAppMessage.MessageContext {
        return InAppMessage.MessageContext(
            defaultLang,
            experimentContext,
            platformTypes,
            orientations,
            messages
        )
    }

    fun message(
        variationKey: String? = null,
        lang: String = "ko",
        images: List<InAppMessage.Message.Image> = listOf(image()),
        imageAutoScroll: InAppMessage.Message.ImageAutoScroll? = null,
        text: InAppMessage.Message.Text? = text(),
        buttons: List<InAppMessage.Message.Button> = listOf(button()),
        closeButton: InAppMessage.Message.Button? = null,
        action: InAppMessage.Action? = null,
        outerButtons: List<InAppMessage.Message.PositionalButton> = emptyList(),
        innerButtons: List<InAppMessage.Message.PositionalButton> = emptyList(),
    ): InAppMessage.Message {
        return InAppMessage.Message(
            variationKey = variationKey,
            lang = lang,
            layout = InAppMessage.Message.Layout(
                displayType = InAppMessage.DisplayType.MODAL,
                layoutType = InAppMessage.LayoutType.IMAGE_ONLY,
                alignment = null
            ),
            images = images,
            imageAutoScroll = imageAutoScroll,
            text = text,
            buttons = buttons,
            closeButton = closeButton,
            background = InAppMessage.Message.Background("#FFFFFF"),
            action = action,
            outerButtons = outerButtons,
            innerButtons = innerButtons
        )
    }

    fun action(
        behavior: InAppMessage.Behavior = InAppMessage.Behavior.CLICK,
        type: InAppMessage.ActionType = InAppMessage.ActionType.CLOSE,
        value: String? = null,
    ): InAppMessage.Action {
        return InAppMessage.Action(
            behavior = behavior,
            actionType = type,
            value = value
        )
    }

    fun button(
        text: String = "button",
        textColor: String = "#000000",
        bgColor: String = "#FFFFFF",
        borderColor: String = "#FFFFFF",
        action: InAppMessage.Action = action(),
    ): InAppMessage.Message.Button {
        return InAppMessage.Message.Button(
            text = text,
            style = InAppMessage.Message.Button.Style(
                textColor = textColor,
                bgColor = bgColor,
                borderColor = borderColor
            ),
            action = action
        )
    }

    fun image(
        orientation: InAppMessage.Orientation = InAppMessage.Orientation.VERTICAL,
        imagePath: String = "image_path",
        action: InAppMessage.Action? = null,
    ): InAppMessage.Message.Image {
        return InAppMessage.Message.Image(
            orientation = orientation,
            imagePath = imagePath,
            action = action
        )
    }

    fun text(
        title: String = "title",
        titleColor: String = "#000000",
        body: String = "body",
        bodyColor: String = "#FFFFFF",
    ): InAppMessage.Message.Text {
        return InAppMessage.Message.Text(
            title = InAppMessage.Message.Text.Attribute(title, InAppMessage.Message.Text.Style(titleColor)),
            body = InAppMessage.Message.Text.Attribute(body, InAppMessage.Message.Text.Style(bodyColor))
        )
    }

    fun eligibilityRequest(
        workspace: Workspace = workspace(),
        user: HackleUser = HackleUser.builder().identifier(IdentifierType.ID, "user").build(),
        inAppMessage: InAppMessage = create(),
        timestamp: Long = System.currentTimeMillis(),
    ): InAppMessageEligibilityRequest {
        return InAppMessageEligibilityRequest(
            workspace = workspace,
            user = user,
            inAppMessage = inAppMessage,
            timestamp = timestamp
        )
    }


    fun eligibilityEvaluation(
        reason: DecisionReason = DecisionReason.IN_APP_MESSAGE_TARGET,
        targetEvaluations: List<Evaluator.Evaluation> = emptyList(),
        inAppMessage: InAppMessage = create(),
        isEligible: Boolean = true,
        layoutEvaluation: InAppMessageLayoutEvaluation? = null,
    ): InAppMessageEligibilityEvaluation {
        return InAppMessageEligibilityEvaluation(
            reason = reason,
            targetEvaluations = targetEvaluations,
            inAppMessage = inAppMessage,
            isEligible = isEligible,
            layoutEvaluation = layoutEvaluation
        )
    }

    fun layoutRequest(
        workspace: Workspace = workspace(),
        user: HackleUser = HackleUser.builder().identifier(IdentifierType.ID, "user").build(),
        inAppMessage: InAppMessage = create(),
    ): InAppMessageLayoutRequest {
        return InAppMessageLayoutRequest(
            workspace = workspace,
            user = user,
            inAppMessage = inAppMessage
        )
    }

    fun layoutEvaluation(
        request: InAppMessageLayoutRequest = layoutRequest(),
        reason: DecisionReason = DecisionReason.IN_APP_MESSAGE_TARGET,
        targetEvaluations: List<Evaluator.Evaluation> = emptyList(),
        message: InAppMessage.Message = request.inAppMessage.messageContext.messages.first(),
        properties: Map<String, Any> = emptyMap(),
    ): InAppMessageLayoutEvaluation {
        return InAppMessageLayoutEvaluation(
            request = request,
            reason = reason,
            targetEvaluations = targetEvaluations,
            message = message,
            properties = properties
        )
    }
}
