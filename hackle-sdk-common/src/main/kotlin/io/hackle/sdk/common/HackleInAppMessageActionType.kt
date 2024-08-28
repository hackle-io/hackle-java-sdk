package io.hackle.sdk.common

/**
 * An enum that represents the type of action that can be performed when InAppMessage is clicked.
 */
enum class HackleInAppMessageActionType {

    /**
     * Closes the InAppMessage.
     */
    CLOSE,

    /**
     * Hides the InAppMessage for a specified period.
     */
    HIDDEN,

    /**
     * Opens a URL.
     */
    LINK,

    /**
     * Opens a URL and then closes the InAppMessage.
     */
    LINK_AND_CLOSE
}
