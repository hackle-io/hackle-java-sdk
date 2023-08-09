package io.hackle.sdk

import io.hackle.sdk.common.HackleRegion
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HackleConfigTest {
    @Test
    fun `build default region`() {
        val config = HackleConfig.builder()
            .region(HackleRegion.DEFAULT)
            .build()

        expectThat(config) {
            get { sdkUrl } isEqualTo "https://sdk.hackle.io"
            get { eventUrl } isEqualTo "https://event.hackle.io"
            get { monitoringUrl } isEqualTo "https://monitoring.hackle.io"
        }
    }

    @Test
    fun `build static region`() {
        val config = HackleConfig.builder()
            .region(HackleRegion.STATIC)
            .build()

        expectThat(config) {
            get { sdkUrl } isEqualTo "https://static-sdk.hackle.io"
            get { eventUrl } isEqualTo "https://static-event.hackle.io"
            get { monitoringUrl } isEqualTo "https://static-monitoring.hackle.io"
        }
    }
}