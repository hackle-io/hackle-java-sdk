package io.hackle.sdk.internal.workspace

import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.scheduler.Schedulers
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.hackle.sdk.core.workspace.config.WorkspaceConfig
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
class PollingWorkspaceConfigFetcherTest {

    @RelaxedMockK
    private lateinit var httpWorkspaceFetcher: HttpWorkspaceConfigFetcher

    private val user = HackleUser.builder().identifier(IdentifierType.ID, "user").build()

    private fun sut(
        httpWorkspaceFetcher: HttpWorkspaceConfigFetcher = this.httpWorkspaceFetcher,
        pollingIntervalMillis: Long = 10000,
        scheduler: Scheduler = Schedulers.executor("test")
    ): PollingWorkspaceConfigFetcher {
        return PollingWorkspaceConfigFetcher(httpWorkspaceFetcher, pollingIntervalMillis, scheduler)
    }

    @DisplayName("workspace()")
    @Nested
    inner class WorkspaceTest {

        @Test
        fun `when before poll then return null`() {
            // given
            val sut = sut()

            // when
            val actual = sut.workspace(user)

            // then
            expectThat(actual).isNull()
        }

        @Test
        fun `when workspace is fetched then return that workspace`() {
            // given
            val workspace = mockk<WorkspaceConfig>()
            every { httpWorkspaceFetcher.fetchIfModified() } returns workspace
            val sut = sut()

            // when
            sut.start()
            val actual = sut.workspace(user)

            // then
            expectThat(actual) isSameInstanceAs workspace
        }
    }

    @DisplayName("metadata()")
    @Nested
    inner class MetadataTest {

        @Test
        fun `when before poll then return null`() {
            // given
            val sut = sut()

            // when
            val actual = sut.metadata()

            // then
            expectThat(actual).isNull()
        }

        @Test
        fun `when workspace is fetched then return the metadata of that workspace`() {
            // given
            val metadata = mockk<WorkspaceConfig.Metadata>()
            val workspace = mockk<WorkspaceConfig> {
                every { this@mockk.metadata } returns metadata
            }
            every { httpWorkspaceFetcher.fetchIfModified() } returns workspace
            val sut = sut()

            // when
            sut.start()
            val actual = sut.metadata()

            // then
            expectThat(actual) isSameInstanceAs metadata
        }
    }

    @DisplayName("poll()")
    @Nested
    inner class PollTest {

        @Test
        fun `fail to poll`() {
            // given
            every { httpWorkspaceFetcher.fetchIfModified() } throws IllegalArgumentException()
            val sut = sut()

            // when
            sut.start()
            val actual = sut.workspace(user)

            // then
            expectThat(actual).isNull()
        }

        @Test
        fun `success to poll`() {
            // given
            val workspace = mockk<WorkspaceConfig>()
            every { httpWorkspaceFetcher.fetchIfModified() } returns workspace
            val sut = sut()

            // when
            sut.start()
            val actual = sut.workspace(user)

            // then
            expectThat(actual) isSameInstanceAs workspace
        }

        @Test
        fun `workspace not modified`() {
            // given
            val workspace = mockk<WorkspaceConfig>()
            every { httpWorkspaceFetcher.fetchIfModified() } returnsMany listOf(workspace, null, null, null)
            val sut = sut(pollingIntervalMillis = 100)

            // when
            sut.start()
            Thread.sleep(350)
            val actual = sut.workspace(user)

            // then
            expectThat(actual) isSameInstanceAs workspace
        }
    }

    @DisplayName("start()")
    @Nested
    inner class Start {

        @Test
        fun `poll`() {
            // given
            val workspace = mockk<WorkspaceConfig>()
            every { httpWorkspaceFetcher.fetchIfModified() } returns workspace
            val sut = sut()

            // when
            sut.start()
            val actual = sut.workspace(user)

            // then
            expectThat(actual) isSameInstanceAs workspace
        }


        @Test
        fun `start scheduling`() {
            // given
            val workspace = mockk<WorkspaceConfig>()
            every { httpWorkspaceFetcher.fetchIfModified() } returns workspace
            val sut = sut(pollingIntervalMillis = 100)

            // when
            sut.start()
            Thread.sleep(550)

            // then
            verify(exactly = 6) {
                httpWorkspaceFetcher.fetchIfModified()
            }
        }

        @Test
        fun `start once`() {
            // given
            val workspace = mockk<WorkspaceConfig>()
            every { httpWorkspaceFetcher.fetchIfModified() } returns workspace
            val sut = sut(pollingIntervalMillis = 100)

            // when
            repeat(10) {
                sut.start()
            }
            Thread.sleep(550)

            // then
            verify(exactly = 6) {
                httpWorkspaceFetcher.fetchIfModified()
            }
        }
    }

    @DisplayName("close()")
    @Nested
    inner class CloseTest {

        @Test
        fun `cancel polling job`() {
            // given
            val workspace = mockk<WorkspaceConfig>()
            every { httpWorkspaceFetcher.fetchIfModified() } returns workspace
            val sut = sut(pollingIntervalMillis = 100)

            // when
            sut.start()
            Thread.sleep(550)
            sut.close()
            Thread.sleep(500)

            // then
            verify(exactly = 6) {
                httpWorkspaceFetcher.fetchIfModified()
            }
        }
    }
}
