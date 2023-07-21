package io.hackle.sdk.core.model

import java.io.Serializable

data class InAppMessage(
    val id: Long,
    val key: Long,
    val status: Status,
    val period: Period,
    val eventTrigger: EventTrigger,
    val targetContext: TargetContext,
    val messageContext: MessageContext
) : Serializable {

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
        MODAL
    }

    enum class LayoutType {
        IMAGE_ONLY,
        IMAGE_TEXT
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
        IMAGE,
        BUTTON,
        X_BUTTON
    }

    sealed class Period : Serializable {
        fun within(timestamp: Long): Boolean {
            return when (this) {
                is Always -> true
                is Custom -> timestamp in startMillisInclusive until endMillisExclusive
            }
        }

        object Always : Period()

        class Custom(
            val startMillisInclusive: Long,
            val endMillisExclusive: Long
        ) : Period()
    }

    data class EventTrigger(
        val rules: List<Rule>,
        val frequencyCap: FrequencyCap?
    ) : Serializable {
        data class Rule(
            val eventKey: String,
            val targets: List<Target>
        ) : Serializable

        data class FrequencyCap(
            val identifierCaps: List<IdentifierCap>,
            val durationCap: DurationCap?
        ) : Serializable

        data class IdentifierCap(
            val identifierType: String,
            val count: Int
        ) : Serializable

        data class DurationCap(
            val durationMillis: Long,
            val count: Int
        ) : Serializable
    }

    data class TargetContext(
        val targets: List<Target>,
        val overrides: List<UserOverride>
    ) : Serializable

    data class UserOverride(
        val identifierType: String,
        val identifiers: List<String>
    ) : Serializable

    data class MessageContext(
        val defaultLang: String,
        val platformTypes: List<PlatformType>,
        val orientations: List<Orientation>,
        val messages: List<Message>
    ) : Serializable

    data class Message(
        val lang: String,
        val layout: Layout,
        val images: List<Image>,
        val text: Text?,
        val buttons: List<Button>,
        val closeButton: Button?,
        val background: Background
    ) : Serializable {

        data class Layout(
            val displayType: DisplayType,
            val layoutType: LayoutType
        ) : Serializable

        data class Image(
            val orientation: Orientation,
            val imagePath: String,
            val action: Action?
        ) : Serializable

        data class Text(
            val title: Attribute,
            val body: Attribute
        ) : Serializable {
            data class Attribute(
                val text: String,
                val style: Style
            ) : Serializable

            data class Style(
                val textColor: String
            ) : Serializable
        }

        data class Button(
            val text: String,
            val style: Style,
            val action: Action
        ) : Serializable {

            data class Style(
                val textColor: String,
                val bgColor: String,
                val borderColor: String
            ) : Serializable
        }

        data class Background(
            val color: String
        ) : Serializable
    }

    data class Action(
        val behavior: Behavior,
        val type: ActionType,
        val value: String?
    ) : Serializable
}

internal fun InAppMessage.supports(platform: InAppMessage.PlatformType): Boolean {
    return platform in messageContext.platformTypes
}


internal operator fun InAppMessage.Period.contains(timestamp: Long): Boolean {
    return within(timestamp)
}