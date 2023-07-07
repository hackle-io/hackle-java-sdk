package io.hackle.sdk.core.model

data class InAppMessage(
    val id: Long,
    val key: Long,
    val displayTimeRange: Range,
    val status: Status,
    val eventTriggerRules: List<EventTriggerRule>,
    val targetContext: TargetContext,
    val messageContext: MessageContext
) {

    sealed class Range {
        fun within(now: Long): Boolean {
            return when (this) {
                is Immediate -> true
                is Custom -> now in startEpochTimeMillis until endEpochTimeMillis
            }
        }

        object Immediate : Range()

        data class Custom(
            val startEpochTimeMillis: Long,
            val endEpochTimeMillis: Long
        ) : Range()
    }

    data class EventTriggerRule(
        val eventKey: String,
        val targets: List<Target>
    )

    data class TargetContext(
        val targets: List<Target>,
        val overrides: List<UserOverride>
    ) {
        data class UserOverride(
            val identifierType: String,
            val identifiers: List<String>
        )
    }

    data class MessageContext(
        val defaultLang: String,
        val platformTypes: List<PlatformType>,
        val orientations: List<Orientation>,
        val exposure: Exposure,
        val messages: List<Message>
    ) {

        enum class PlatformType {
            ANDROID, IOS, WEB;
        }

        enum class Orientation {
            VERTICAL, HORIZONTAL;
        }

        data class Exposure(
            val type: Type,
            val key: Long?
        ) {
            enum class Type {
                DEFAULT, AB_TEST
            }
        }

        data class Message(
            val lang: String,
            val layout: Layout,
            val images: List<Image>,
            val text: Text?,
            val buttons: List<Button>,
            val background: Background,
            val closeButton: CloseButton?
        ) {

            data class Layout(
                val displayType: DisplayType,
                val layoutType: LayoutType
            ) {
                enum class DisplayType {
                    MODAL
                }

                enum class LayoutType {
                    IMAGE_ONLY,
                    IMAGE_TEXT
                }
            }

            data class Image(
                val orientation: Orientation,
                val imagePath: String,
                val action: Action?
            )

            data class Text(
                val title: TextAttribute,
                val body: TextAttribute
            ) {
                data class TextAttribute(
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

            data class CloseButton(
                val style: Style,
                val action: Action
            ) {
                data class Style(
                    val color: String
                )
            }
        }

        data class Action(
            val behavior: Behavior,
            val type: Type,
            val value: String?
        ) {

            enum class Behavior {
                CLICK
            }

            enum class Type {
                WEB_LINK,
                CLOSE,
                HIDDEN;
            }

        }
    }

    enum class TimeUnitType {
        CUSTOM, IMMEDIATE;
    }

    enum class Status {
        INITIALIZED,
        DRAFT,
        ACTIVE,
        PAUSE,
        FINISH,
        ARCHIVED;
    }
}
