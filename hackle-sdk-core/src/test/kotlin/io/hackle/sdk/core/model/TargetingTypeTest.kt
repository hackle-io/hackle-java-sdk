package io.hackle.sdk.core.model

import io.hackle.sdk.core.internal.utils.safe
import io.hackle.sdk.core.model.Target.Key.Type.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class TargetingTypeTest {
    @Test
    fun `IDENTIFIER`() {
        for (keyType in Target.Key.Type.values()) {
            val targetingType = TargetingType.IDENTIFIER
            when (keyType) {
                SEGMENT -> assertTrue(targetingType.supports(keyType))
                USER_ID,
                USER_PROPERTY,
                HACKLE_PROPERTY -> assertFalse(targetingType.supports(keyType))

                AB_TEST,
                FEATURE_FLAG -> assertFalse(targetingType.supports(keyType))

                EVENT_PROPERTY -> assertFalse(targetingType.supports(keyType))
                COHORT -> assertFalse(targetingType.supports(keyType))

                NUMBER_OF_EVENTS_IN_DAYS -> assertFalse(targetingType.supports(keyType))
                NUMBER_OF_EVENTS_WITH_PROPERTY_IN_DAYS -> assertFalse(targetingType.supports(keyType))
            }.safe
        }
    }

    @Test
    fun `PROPERTY`() {
        for (keyType in Target.Key.Type.values()) {
            val targetingType = TargetingType.PROPERTY
            when (keyType) {
                SEGMENT,
                USER_PROPERTY,
                HACKLE_PROPERTY,
                AB_TEST,
                EVENT_PROPERTY,
                FEATURE_FLAG,
                COHORT,
                NUMBER_OF_EVENTS_IN_DAYS,
                NUMBER_OF_EVENTS_WITH_PROPERTY_IN_DAYS -> assertTrue(targetingType.supports(keyType))

                USER_ID -> assertFalse(targetingType.supports(keyType))

            }.safe
        }
    }

    @Test
    fun `SEGMENT`() {
        for (keyType in Target.Key.Type.values()) {
            val targetingType = TargetingType.SEGMENT
            when (keyType) {
                USER_PROPERTY,
                HACKLE_PROPERTY,
                USER_ID,
                COHORT,
                NUMBER_OF_EVENTS_IN_DAYS,
                NUMBER_OF_EVENTS_WITH_PROPERTY_IN_DAYS -> assertTrue(targetingType.supports(keyType))

                SEGMENT -> assertFalse(targetingType.supports(keyType))
                AB_TEST,
                FEATURE_FLAG,
                EVENT_PROPERTY -> assertFalse(targetingType.supports(keyType))
            }.safe
        }
    }
}
