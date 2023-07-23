package io.hackle.sdk.core.model

data class InAppMessage(
    val id: Long,
    val key: Long,
    val status: Status,
    val period: Period,
    val eventTrigger: EventTrigger,
    val targetContext: TargetContext,
    val messageContext: MessageContext
) {

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
            val endMillisExclusive: Long
        ) : Period()
    }

    data class EventTrigger(
        val rules: List<Rule>,
        val frequencyCap: FrequencyCap?
    ) {
        data class Rule(
            val eventKey: String,
            val targets: List<Target>
        )

        data class FrequencyCap(
            val identifierCaps: List<IdentifierCap>,
            val durationCap: DurationCap?
        )

        data class IdentifierCap(
            val identifierType: String,
            val count: Int
        )

        data class DurationCap(
            val durationMillis: Long,
            val count: Int
        )
    }

    data class TargetContext(
        val targets: List<Target>,
        val overrides: List<UserOverride>
    )

    data class UserOverride(
        val identifierType: String,
        val identifiers: List<String>
    )

    data class MessageContext(
        val defaultLang: String,
        val platformTypes: List<PlatformType>,
        val orientations: List<Orientation>,
        val messages: List<Message>
    )

    data class Message(
        val lang: String,
        val layout: Layout,
        val images: List<Image>,
        val text: Text?,
        val buttons: List<Button>,
        val closeButton: Button?,
        val background: Background
    ) {

        data class Layout(
            val displayType: DisplayType,
            val layoutType: LayoutType
        )

        data class Image(
            val orientation: Orientation,
            val imagePath: String,
            val action: Action?
        )

        data class Text(
            val title: Attribute,
            val body: Attribute
        ) {
            data class Attribute(
                val text: String,
                val style: Style
            )

            data class Style(
                val textColor: String
            )
        }

        data class Button(
            val text: String,
            val style: Style,
            val action: Action
        ) {

            data class Style(
                val textColor: String,
                val bgColor: String,
                val borderColor: String
            )
        }

        data class Background(
            val color: String
        )
    }

    data class Action(
        val behavior: Behavior,
        val type: ActionType,
        val value: String?
    )
}

internal fun InAppMessage.supports(platform: InAppMessage.PlatformType): Boolean {
    return platform in messageContext.platformTypes
}


internal operator fun InAppMessage.Period.contains(timestamp: Long): Boolean {
    return within(timestamp)
}