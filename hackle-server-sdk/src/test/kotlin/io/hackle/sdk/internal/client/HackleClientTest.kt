package io.hackle.sdk.internal.client

import io.hackle.sdk.core.HackleCore
import io.hackle.sdk.core.client
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.core.workspace.WorkspaceFetcher
import io.hackle.sdk.internal.user.HackleUserResolver
import io.hackle.sdk.internal.utils.parseJson
import io.hackle.sdk.internal.workspace.WorkspaceImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.nio.file.Files
import java.nio.file.Paths

internal class HackleClientTest {

    private lateinit var sut: HackleClientImpl

    @BeforeEach
    fun beforeEach() {
        val internalClient = HackleCore.client(
            workspaceFetcher = StaticResourceWorkspaceFetcher,
            eventProcessor = NoopEventProcess
        )
        sut = HackleClientImpl(internalClient, HackleUserResolver())
    }

    @Test
    fun `asdf`() {
        // given


        val actual = sut.variationDetail(5, "a")


        expectThat(actual) {
            get { getString("string_key_1", "!!") } isEqualTo "string_value_1"
            get { getInt("int_key_1", -1) } isEqualTo 2147483647
        }
        // when

        // then
    }

    private object StaticResourceWorkspaceFetcher : WorkspaceFetcher {
        private val WORKSPACE =
            WorkspaceImpl.from(String(Files.readAllBytes(Paths.get("src/test/resources/workspace_config.json"))).parseJson())

        override fun fetch(): Workspace = WORKSPACE
    }

    private object NoopEventProcess : EventProcessor {
        override fun process(event: UserEvent) {
        }
    }
}