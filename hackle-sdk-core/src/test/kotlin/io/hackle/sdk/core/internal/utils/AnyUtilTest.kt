package io.hackle.sdk.core.internal.utils

import io.hackle.sdk.core.model.Experiment
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class AnyUtilTest {

    @Test
    fun `AutoCloseable 인경우 close를 호출한다`() {
        val sut = mockk<AutoCloseable>(relaxUnitFun = true)
        sut.tryClose()
        verify(exactly = 1) {
            sut.close()
        }
    }

    @Test
    fun `AutoCloseable을 닫다가 예외가 발생해도 무시한다`() {
        val sut = mockk<AutoCloseable>(relaxUnitFun = true) {
            every { close() } throws Exception()
        }
        sut.tryClose()
        verify(exactly = 1) {
            sut.close()
        }
    }

    @Test
    fun `safe`() {
        val type: Experiment.Type = Experiment.Type.AB_TEST
        when (type) {
            Experiment.Type.AB_TEST -> Unit
            Experiment.Type.FEATURE_FLAG -> Unit
        }.safe
    }
}
