package io.hackle.sdk

class HackleConfig private constructor(builder: Builder) {

    val sdkUrl: String = builder.sdkUrl
    val eventUrl: String = builder.eventUrl
    val monitoringUrl: String = builder.monitoringUrl

    class Builder {

        internal var sdkUrl: String = DEFAULT_SDK_URL
        internal var eventUrl: String = DEFAULT_EVENT_URL
        internal var monitoringUrl: String = DEFAULT_MONITORING_URL

        fun sdkUrl(sdkUrl: String) = apply {
            this.sdkUrl = sdkUrl
        }

        fun eventUrl(eventUrl: String) = apply {
            this.eventUrl = eventUrl
        }

        fun monitoringUrl(monitoringUrl: String) = apply {
            this.monitoringUrl = monitoringUrl
        }

        fun build(): HackleConfig {
            return HackleConfig(this)
        }
    }

    companion object {

        internal const val DEFAULT_SDK_URL = "https://sdk.hackle.io"
        internal const val DEFAULT_EVENT_URL = "https://event.hackle.io"
        internal const val DEFAULT_MONITORING_URL = "https://monitoring.hackle.io"

        val DEFAULT: HackleConfig = builder().build()

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}