package io.hackle.sdk.core.internal.metrics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class MetricTest {

    @Test
    fun `metric equals`() {
        val m1 = MetricStub(Metric.Id("id_1", emptyMap(), Metric.Type.COUNTER))
        val m11 = MetricStub(Metric.Id("id_1", emptyMap(), Metric.Type.COUNTER))
        val m2 = MetricStub(Metric.Id("id_2", emptyMap(), Metric.Type.COUNTER))

        assertEquals(m1, m1)
        assertEquals(m1, m11)
        assertNotEquals(m1, null)
        assertNotEquals(m1, "hello")
        assertNotEquals(m1, m2)
    }

    @Test
    fun `metric hashcode`() {
        val id = Metric.Id("id_1", emptyMap(), Metric.Type.COUNTER)
        val metric = MetricStub(id)
        assertEquals(id.hashCode(), metric.hashCode())
    }


    @Test
    fun `metric id`() {

        val id = Metric.Id("counter", emptyMap(), Metric.Type.COUNTER).apply { toString() }
        assertEquals(id, id)

        assertNotEquals(
            Metric.Id("counter", emptyMap(), Metric.Type.COUNTER),
            null
        )

        assertNotEquals(
            Metric.Id("counter", emptyMap(), Metric.Type.COUNTER),
            "hello"
        )

        assertNotEquals(
            Metric.Id("counter", emptyMap(), Metric.Type.COUNTER),
            Metric.Id("counter", mapOf("tag" to "42"), Metric.Type.COUNTER)
        )

        assertEquals(
            Metric.Id("counter", emptyMap(), Metric.Type.COUNTER),
            Metric.Id("counter", emptyMap(), Metric.Type.COUNTER)
        )

        assertEquals(
            Metric.Id("counter", emptyMap(), Metric.Type.COUNTER),
            Metric.Id("counter", emptyMap(), Metric.Type.TIMER)
        )
    }

    private class MetricStub(id: Metric.Id) : AbstractMetric(id) {
        override fun measure(): List<Measurement> = emptyList()
    }
}