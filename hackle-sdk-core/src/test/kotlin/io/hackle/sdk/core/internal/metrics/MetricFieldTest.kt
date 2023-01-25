package io.hackle.sdk.core.internal.metrics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MetricFieldTest {
    @Test
    fun `tagKey`() {
        assertEquals("count", MetricField.COUNT.tagKey)
        assertEquals("total", MetricField.TOTAL.tagKey)
        assertEquals("max", MetricField.MAX.tagKey)
        assertEquals("mean", MetricField.MEAN.tagKey)
    }
}