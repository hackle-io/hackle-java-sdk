package io.hackle.sdk

class HackleConfig private constructor(builder: Builder) {

    val sdkUrl: String = builder.sdkUrl
    val eventUrl: String = builder.eventUrl
    val monitoringUrl: String = builder.monitoringUrl

    class Builder {

        internal var sdkUrl: String = ServerZone.DEFAULT.sdkUrl
        internal var eventUrl: String = ServerZone.DEFAULT.eventUrl
        internal var monitoringUrl: String = ServerZone.DEFAULT.monitoringUrl

        fun sdkUrl(sdkUrl: String) = apply {
            this.sdkUrl = sdkUrl
        }

        fun eventUrl(eventUrl: String) = apply {
            this.eventUrl = eventUrl
        }

        fun monitoringUrl(monitoringUrl: String) = apply {
            this.monitoringUrl = monitoringUrl
        }

        fun serverZone(serverZone: ServerZone) = apply {
            sdkUrl(serverZone.sdkUrl)
            eventUrl(serverZone.eventUrl)
            monitoringUrl(serverZone.monitoringUrl)
        }

        fun build(): HackleConfig {
            return HackleConfig(this)
        }
    }

    companion object {

        val DEFAULT: HackleConfig = builder().build()

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}