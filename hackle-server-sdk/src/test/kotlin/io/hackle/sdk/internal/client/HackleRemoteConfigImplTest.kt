package io.hackle.sdk.internal.client

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.HackleCore
import io.hackle.sdk.internal.user.HackleUserResolver
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class HackleRemoteConfigImplTest {

    @Test
    fun `invalid user`() {
        val user = User.builder().build()
        val core = core(RemoteConfigDecision.of("fail", DecisionReason.DEFAULT_RULE))
        val sut = HackleRemoteConfigImpl(user, core, HackleUserResolver())

        val actual = sut.getString("42", "default")

        expectThat(actual) isEqualTo "default"
    }

    @Test
    fun `string`() {
        val user = User.builder().id("user").build()
        val core = core(RemoteConfigDecision.of("string", DecisionReason.DEFAULT_RULE))
        val sut = HackleRemoteConfigImpl(user, core, HackleUserResolver())

        expectThat(sut.getString("42", "default")) isEqualTo "string"
    }

    @Test
    fun `number`() {
        val user = User.builder().id("user").build()
        val core = core(RemoteConfigDecision.of(42, DecisionReason.DEFAULT_RULE))
        val sut = HackleRemoteConfigImpl(user, core, HackleUserResolver())

        expectThat(sut.getInt("42", 320)) isEqualTo 42
        expectThat(sut.getLong("42", 320L)) isEqualTo 42L
        expectThat(sut.getDouble("42", 320.32)) isEqualTo 42.0
    }

    @Test
    fun `boolean`() {
        val user = User.builder().id("user").build()
        val core = core(RemoteConfigDecision.of(true, DecisionReason.DEFAULT_RULE))
        val sut = HackleRemoteConfigImpl(user, core, HackleUserResolver())

        expectThat(sut.getBoolean("42", false)) isEqualTo true
    }

    @Test
    fun `exception`() {
        val user = User.builder().id("user").build()
        val core = mockk<HackleCore> {
            every { remoteConfig(any(), any(), any(), any()) } throws IllegalArgumentException()
        }
        val sut = HackleRemoteConfigImpl(user, core, HackleUserResolver())

        expectThat(sut.getString("42", "default")) isEqualTo "default"
    }

    private inline fun <reified T : Any> core(decision: RemoteConfigDecision<T>): HackleCore {
        return mockk {
            every { remoteConfig(any(), any(), any(), any<T>()) } returns decision
        }
    }
}