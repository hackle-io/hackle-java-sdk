package io.hackle.sdk.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class HackleCoreContextTest {

    @Test
    fun `등록되지 않은 타입을 get 하면 예외 발생`() {
        val sut = HackleCoreContext.create()

        assertThrows<IllegalArgumentException> { sut.get<HackleCoreContextTest>() }
        expectThat(sut.getOrNull<HackleCoreContextTest>()).isNull()
    }

    @Test
    fun `인스턴스를 등록하고 타입으로 가져온다`() {
        val sut = HackleCoreContext.create()

        val instance = HackleCoreContextTest()
        val registered = sut.register(instance)

        expectThat(registered) isSameInstanceAs instance
        expectThat(sut.get<HackleCoreContextTest>()) isSameInstanceAs instance
        expectThat(sut.getOrNull<HackleCoreContextTest>()) isSameInstanceAs instance
    }

    @Test
    fun `상위 타입으로도 가져올 수 있다`() {
        val sut = HackleCoreContext.create()

        val instance = sut.register("hello")

        expectThat(sut.getOrNull<CharSequence>()).isNotNull() isSameInstanceAs instance
    }
}
