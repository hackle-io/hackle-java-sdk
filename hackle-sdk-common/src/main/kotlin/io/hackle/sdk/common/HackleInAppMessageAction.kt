package io.hackle.sdk.common

/**
 * Represents the action performed when InAppMessage is clicked.
 */
interface HackleInAppMessageAction {

    /**
     * The type of action to be performed.
     */
    val type: HackleInAppMessageActionType

    /**
     * An optional close action, present when the action type is 'CLOSE'.
     */
    val close: Close?

    /**
     * An optional link action, present when the action type is 'LINK'.
     */
    val link: Link?

    /**
     * Represents the close action for an InAppMessage.
     */
    interface Close {

        /**
         * The duration in milliseconds after which the InAppMessage should be hidden.
         * If null, the InAppMessage will not be hidden.
         */
        val hideDurationMillis: Long?
    }

    /**
     * Represents the link action for an InAppMessage.
     */
    interface Link {

        /**
         * The URL to be opened when the link action is triggered.
         */
        val url: String

        /**
         * Indicates whether the InAppMessage should be closed after the link is opened.
         */
        val shouldCloseAfterLink: Boolean
    }
}
