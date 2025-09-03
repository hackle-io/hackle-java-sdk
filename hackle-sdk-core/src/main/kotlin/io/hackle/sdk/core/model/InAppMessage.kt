package io.hackle.sdk.core.model

import io.hackle.sdk.common.HackleInAppMessage
import io.hackle.sdk.common.HackleInAppMessageAction
import io.hackle.sdk.common.HackleInAppMessageActionType

data class InAppMessage(
    val id: Long,
    override val key: Long,
    val status: Status,
    val period: Period,
    val eventTrigger: EventTrigger,
    val evaluateContext: EvaluateContext,
    val targetContext: TargetContext,
    val messageContext: MessageContext,
) : HackleInAppMessage {

    enum class Status {
        INITIALIZED,
        DRAFT,
        ACTIVE,
        PAUSE,
        FINISH,
        ARCHIVED;
    }

    enum class PlatformType {
        ANDROID, IOS, WEB;
    }

    enum class Orientation {
        VERTICAL, HORIZONTAL;
    }

    enum class DisplayType {
        NONE, MODAL, BANNER, BOTTOM_SHEET;
    }

    enum class LayoutType {
        NONE,
        IMAGE_ONLY,
        IMAGE_TEXT,
        TEXT_ONLY,
        IMAGE
    }

    enum class Behavior {
        CLICK
    }

    enum class ActionType {
        WEB_LINK,
        CLOSE,
        HIDDEN,
        LINK_AND_CLOSE;
    }

    enum class ActionArea {
        MESSAGE,
        IMAGE,
        BUTTON,
        X_BUTTON
    }

    sealed class Period {
        fun within(timestamp: Long): Boolean {
            return when (this) {
                is Always -> true
                is Custom -> timestamp in startMillisInclusive until endMillisExclusive
            }
        }

        object Always : Period()

        class Custom(
            val startMillisInclusive: Long,
            val endMillisExclusive: Long,
        ) : Period()
    }

    data class EventTrigger(
        val rules: List<Rule>,
        val frequencyCap: FrequencyCap?,
        val delay: Delay,
    ) {
        data class Rule(
            val eventKey: String,
            val targets: List<Target>,
        )

        data class FrequencyCap(
            val identifierCaps: List<IdentifierCap>,
            val durationCap: DurationCap?,
        )

        data class IdentifierCap(
            val identifierType: String,
            val count: Int,
        )

        data class DurationCap(
            val durationMillis: Long,
            val count: Int,
        )
    }

    data class Delay(
        val type: Type,
        val afterCondition: AfterCondition?,
    ) {

        enum class Type {
            IMMEDIATE, AFTER;
        }

        data class AfterCondition(
            val durationMillis: Long,
        )

        fun deliverAt(startedAt: Long): Long {
            return when (type) {
                Type.IMMEDIATE -> startedAt
                Type.AFTER -> startedAt + requireNotNull(afterCondition).durationMillis
            }
        }
    }

    data class TargetContext(
        val targets: List<Target>,
        val overrides: List<UserOverride>,
    )

    data class UserOverride(
        val identifierType: String,
        val identifiers: List<String>,
    )

    data class EvaluateContext(
        val atDeliverTime: Boolean,
    )

    data class ExperimentContext(
        val key: Long,
    )

    data class MessageContext(
        val defaultLang: String,
        val experimentContext: ExperimentContext?,
        val platformTypes: List<PlatformType>,
        val orientations: List<Orientation>,
        val messages: List<Message>,
    )

    data class Message(
        val variationKey: String?,
        val lang: String,
        val layout: Layout,
        val images: List<Image>,
        val imageAutoScroll: ImageAutoScroll?,
        val text: Text?,
        val buttons: List<Button>,
        val closeButton: Button?,
        val background: Background,
        val action: Action?,
        val outerButtons: List<PositionalButton>,
        val innerButtons: List<PositionalButton>,
    ) {
        data class Alignment(
            val horizontal: Horizontal,
            val vertical: Vertical,
        ) {
            enum class Horizontal {
                LEFT, CENTER, RIGHT
            }

            enum class Vertical {
                TOP, MIDDLE, BOTTOM
            }
        }

        data class Layout(
            val displayType: DisplayType,
            val layoutType: LayoutType,
            val alignment: Alignment?,
        )

        data class Image(
            val orientation: Orientation,
            val imagePath: String,
            val action: Action?,
        )

        data class ImageAutoScroll(
            val intervalMillis: Long,
        )

        data class Text(
            val title: Attribute,
            val body: Attribute,
        ) {
            data class Attribute(
                val text: String,
                val style: Style,
            )

            data class Style(
                val textColor: String,
            )
        }

        data class Button(
            val text: String,
            val style: Style,
            val action: Action,
        ) {

            data class Style(
                val textColor: String,
                val bgColor: String,
                val borderColor: String,
            )
        }

        data class PositionalButton(
            val button: Button,
            val alignment: Alignment,
        )

        data class Background(
            val color: String,
        )
    }

    data class Action(
        val behavior: Behavior,
        val actionType: ActionType,
        val value: String?,
    ) : HackleInAppMessageAction {

        override val type: HackleInAppMessageActionType
            get() = when (actionType) {
                ActionType.CLOSE, ActionType.HIDDEN -> HackleInAppMessageActionType.CLOSE
                ActionType.WEB_LINK, ActionType.LINK_AND_CLOSE -> HackleInAppMessageActionType.LINK
            }

        override val close: HackleInAppMessageAction.Close? by lazy {
            when (actionType) {
                ActionType.CLOSE -> InAppMessageCloseAction(null)
                ActionType.HIDDEN -> InAppMessageCloseAction(DEFAULT_HIDE_DURATION_MILLIS)
                ActionType.WEB_LINK, ActionType.LINK_AND_CLOSE -> null
            }
        }

        override val link: HackleInAppMessageAction.Link? by lazy {
            when (actionType) {
                ActionType.CLOSE, ActionType.HIDDEN -> null
                ActionType.WEB_LINK -> InAppMessageLinkAction(requireNotNull(value), false)
                ActionType.LINK_AND_CLOSE -> InAppMessageLinkAction(requireNotNull(value), true)
            }
        }

        companion object {
            const val DEFAULT_HIDE_DURATION_MILLIS: Long = 1000 * 60 * 60 * 24 // 24H
        }
    }

    private data class InAppMessageCloseAction(
        override val hideDurationMillis: Long?,
    ) : HackleInAppMessageAction.Close

    private data class InAppMessageLinkAction(
        override val url: String,
        override val shouldCloseAfterLink: Boolean,
    ) : HackleInAppMessageAction.Link

    override fun toString(): String {
        return "InAppMessage(id=$id, key=$key, status=$status)"
    }
}

internal fun InAppMessage.supports(platform: InAppMessage.PlatformType): Boolean {
    return platform in messageContext.platformTypes
}


internal operator fun InAppMessage.Period.contains(timestamp: Long): Boolean {
    return within(timestamp)
}
