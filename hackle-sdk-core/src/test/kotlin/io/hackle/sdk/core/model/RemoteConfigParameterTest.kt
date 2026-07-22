package io.hackle.sdk.core.model

import io.hackle.sdk.core.support.RemoteConfigs
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class RemoteConfigParameterTest {

    @Test
    fun `equalsAndHashCode`() {
        val p1 = RemoteConfigs.config(id = 1)
        val p11 = RemoteConfigs.config(id = 1)
        val p2 = RemoteConfigs.config(id = 2)
        assertTrue(p1 == p1)
        assertTrue(p1 == p11)
        assertTrue(p1 != p2)
        assertTrue(!p1.equals("p2"))

        assertTrue(p1.hashCode() == p1.hashCode())
        assertTrue(p1.hashCode() == p11.hashCode())
        assertTrue(p1.hashCode() != p2.hashCode())
    }
}
