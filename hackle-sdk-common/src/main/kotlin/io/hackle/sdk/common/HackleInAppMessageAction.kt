package io.hackle.sdk.common

/**
 * An interface that represents the action performed when InAppMessage is clicked.
 */
interface HackleInAppMessageAction {
    /**
     * The type of action to be performed.
     */
    val type: HackleInAppMessageActionType

    /**
     * The URL to open in case of link-related action.
     */
    val url: String?
}
