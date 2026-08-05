package io.hackle.sdk.core.model

import io.hackle.sdk.core.evaluation.EvaluationPhase
import io.hackle.sdk.core.evaluation.EvaluationPhase.RUNTIME
import io.hackle.sdk.core.evaluation.EvaluationPhase.SYNC
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HacklePropertyTest {

    @Test
    fun `모든 등록 속성은 RUNTIME 을 지원한다`() {
        // UserConditionMatcher 가 phase 게이트를 SYNC 에만 적용하는 전제.
        // SYNC 전용 속성이 추가되면 RUNTIME 평가에도 게이트가 필요해지므로 이 테스트가 먼저 깨져야 한다.
        for (property in HackleProperty.values()) {
            assertTrue(property.supports(RUNTIME), "$property must support RUNTIME")
        }
    }

    @Test
    fun `RUNTIME 전용 속성은 SYNC 를 지원하지 않는다`() {
        for (key in RUNTIME_ONLY_KEYS) {
            assertTrue(HackleProperty.supports(key, RUNTIME), "$key must support RUNTIME")
            assertFalse(HackleProperty.supports(key, SYNC), "$key must not support SYNC")
        }
    }

    @Test
    fun `SYNC 지원 속성은 RUNTIME 과 SYNC 모두 지원한다`() {
        for (key in SYNC_SUPPORTED_KEYS) {
            assertTrue(HackleProperty.supports(key, RUNTIME), "$key must support RUNTIME")
            assertTrue(HackleProperty.supports(key, SYNC), "$key must support SYNC")
        }
    }

    @Test
    fun `phase 분류 목록이 전체 속성을 덮는다`() {
        // 속성이 추가되면 phase 를 어느 쪽으로 분류할지 결정하도록 강제한다.
        // 서버 userHash 필터가 같은 supports 를 쓰기 때문에 분류 누락은 평가와 hash 의 불일치로 이어진다.
        assertEquals(
            HackleProperty.values().size,
            RUNTIME_ONLY_KEYS.size + SYNC_SUPPORTED_KEYS.size
        )
    }

    @Test
    fun `미등록 키는 모든 phase 에서 지원하지 않는다`() {
        for (phase in EvaluationPhase.values()) {
            assertFalse(HackleProperty.supports(UNREGISTERED_KEY, phase), "$phase must not support unregistered key")
        }
    }

    @Test
    fun `미등록 키는 from 으로 해석되지 않는다`() {
        assertNull(HackleProperty.from(UNREGISTERED_KEY))
    }

    @Test
    fun `등록 키는 from 으로 해석된다`() {
        for (key in RUNTIME_ONLY_KEYS + SYNC_SUPPORTED_KEYS) {
            assertNotNull(HackleProperty.from(key), "$key must be resolved")
        }
    }

    companion object {
        private const val UNREGISTERED_KEY = "unregistered_property"

        private val RUNTIME_ONLY_KEYS = listOf(
            "pagePath",
            "url",
            "pageTitle",
            "queryParameter",
            "referrer",
            "screenName",
            "screenClass",
            "host",
            "protocol",
            "orientation",
            "screenWidth",
            "screenHeight",
        )

        private val SYNC_SUPPORTED_KEYS = listOf(
            "platform",
            "osName",
            "osVersion",
            "deviceModel",
            "deviceType",
            "deviceBrand",
            "deviceManufacturer",
            "deviceVendor",
            "locale",
            "language",
            "timeZone",
            "isApp",
            "packageName",
            "versionName",
            "versionCode",
            "browserName",
            "browserMajorVersion",
            "browserVersion",
            "userAgent",
        )
    }
}
