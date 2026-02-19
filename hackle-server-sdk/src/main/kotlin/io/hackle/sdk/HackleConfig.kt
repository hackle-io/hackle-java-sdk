package io.hackle.sdk

import io.hackle.sdk.common.HackleRegion

class HackleConfig private constructor(builder: Builder) {

    val sdkUrl: String = builder.sdkUrl
    val eventUrl: String = builder.eventUrl
    val monitoringUrl: String = builder.monitoringUrl
    val enableMonitoring: Boolean = builder.enableMonitoring

    class Builder {

        internal var sdkUrl: String = HackleRegion.DEFAULT.sdkUrl
        internal var eventUrl: String = HackleRegion.DEFAULT.eventUrl
        internal var monitoringUrl: String = HackleRegion.DEFAULT.monitoringUrl
        internal var enableMonitoring: Boolean = true

        fun sdkUrl(sdkUrl: String) = apply {
            this.sdkUrl = sdkUrl
        }

        fun eventUrl(eventUrl: String) = apply {
            this.eventUrl = eventUrl
        }

        fun monitoringUrl(monitoringUrl: String) = apply {
            this.monitoringUrl = monitoringUrl
        }

        fun enableMonitoring(enableMonitoring: Boolean) = apply {
            this.enableMonitoring = enableMonitoring
        }

        fun region(region: HackleRegion) = apply {
            sdkUrl(region.sdkUrl)
            eventUrl(region.eventUrl)
            monitoringUrl(region.monitoringUrl)
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