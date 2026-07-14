package io.hackle.sdk.core.model

import io.hackle.sdk.core.evaluation.EvaluationPhase
import io.hackle.sdk.core.evaluation.EvaluationPhase.RUNTIME
import io.hackle.sdk.core.evaluation.EvaluationPhase.SYNC

enum class HackleProperty(
    private val key: String,
    private val phases: Set<EvaluationPhase>,
) {
    // RUNTIME + SYNC
    PLATFORM("platform", RUNTIME, SYNC),
    OS_NAME("osName", RUNTIME, SYNC),
    OS_VERSION("osVersion", RUNTIME, SYNC),
    DEVICE_MODEL("deviceModel", RUNTIME, SYNC),
    DEVICE_TYPE("deviceType", RUNTIME, SYNC),
    DEVICE_BRAND("deviceBrand", RUNTIME, SYNC),
    DEVICE_MANUFACTURER("deviceManufacturer", RUNTIME, SYNC),
    DEVICE_VENDOR("deviceVendor", RUNTIME, SYNC),
    LOCALE("locale", RUNTIME, SYNC),
    LANGUAGE("language", RUNTIME, SYNC),
    TIME_ZONE("timeZone", RUNTIME, SYNC),
    IS_APP("isApp", RUNTIME, SYNC),
    PACKAGE_NAME("packageName", RUNTIME, SYNC),
    VERSION_NAME("versionName", RUNTIME, SYNC),
    VERSION_CODE("versionCode", RUNTIME, SYNC),
    BROWSER_NAME("browserName", RUNTIME, SYNC),
    BROWSER_MAJOR_VERSION("browserMajorVersion", RUNTIME, SYNC),
    BROWSER_VERSION("browserVersion", RUNTIME, SYNC),
    USER_AGENT("userAgent", RUNTIME, SYNC),

    // RUNTIME only
    PAGE_PATH("pagePath", RUNTIME),
    URL("url", RUNTIME),
    PAGE_TITLE("pageTitle", RUNTIME),
    QUERY_PARAMETER("queryParameter", RUNTIME),
    REFERRER("referrer", RUNTIME),
    SCREEN_NAME("screenName", RUNTIME),
    SCREEN_CLASS("screenClass", RUNTIME),
    HOST("host", RUNTIME),
    PROTOCOL("protocol", RUNTIME),
    ORIENTATION("orientation", RUNTIME),
    SCREEN_WIDTH("screenWidth", RUNTIME),
    SCREEN_HEIGHT("screenHeight", RUNTIME),
    ;

    constructor(key: String, vararg phases: EvaluationPhase) : this(key, phases.toHashSet())

    fun supports(phase: EvaluationPhase): Boolean {
        return phase in phases
    }

    companion object {
        private val VALUES = values().associateBy { it.key }

        fun from(key: String): HackleProperty? {
            return VALUES[key]
        }

        fun from(key: Target.Key): HackleProperty? {
            if (key.type != Target.Key.Type.HACKLE_PROPERTY) {
                return null
            }
            return from(key.name)
        }
    }
}
