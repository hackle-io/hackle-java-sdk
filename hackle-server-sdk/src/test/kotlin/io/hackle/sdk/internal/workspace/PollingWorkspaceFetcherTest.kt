package io.hackle.sdk.internal.workspace

import io.hackle.sdk.core.internal.scheduler.Scheduler
import io.hackle.sdk.core.internal.scheduler.Schedulers
import io.hackle.sdk.core.workspace.Workspace
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
class PollingWorkspaceFetcherTest {

    @RelaxedMockK
    private lateinit var httpWorkspaceFetcher: HttpWorkspaceFetcher

    private fun sut(
        httpWorkspaceFetcher: HttpWorkspaceFetcher = this.httpWorkspaceFetcher,
        pollingIntervalMillis: Long = 10000,
        scheduler: Scheduler = Schedulers.executor("test")
    ): PollingWorkspaceFetcher {
        return PollingWorkspaceFetcher(httpWorkspaceFetcher, pollingIntervalMillis, scheduler)
    }

    @DisplayName("fetch()")
    @Nested
    inner class FetchTest {

        @Test
        fun `when before poll then return null`() {
            // given
            val sut = sut()

            // when
            val actual = sut.fetch()

            // then
            expectThat(actual).isNull()
        }

        @Test
        fun `when workspace is fetched then return that workspace`() {
            // given
            val workspace = mockk<Workspace>()
            every { httpWorkspaceFetcher.fetchIfModified() } returns workspace
            val sut = sut()

            // when
            sut.start()
            val actual = sut.fetch()

            // then
            expectThat(actual) isSameInstanceAs workspace
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
            val actual = sut.fetch()

            // then
            expectThat(actual).isNull()
        }

        @Test
        fun `success to poll`() {
            // given
            val workspace = mockk<Workspace>()
            every { httpWorkspaceFetcher.fetchIfModified() } returns workspace
            val sut = sut()

            // when
            sut.start()
            val actual = sut.fetch()

            // then
            expectThat(actual) isSameInstanceAs workspace
        }

        @Test
        fun `workspace not modified`() {
            // given
            val workspace = mockk<Workspace>()
            every { httpWorkspaceFetcher.fetchIfModified() } returnsMany listOf(workspace, null, null, null)
            val sut = sut(pollingIntervalMillis = 100)

            // when
            sut.start()
            Thread.sleep(350)
            val actual = sut.fetch()

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
            val workspace = mockk<Workspace>()
            every { httpWorkspaceFetcher.fetchIfModified() } returns workspace
            val sut = sut()

            // when
            sut.start()
            val actual = sut.fetch()

            // then
            expectThat(actual) isSameInstanceAs workspace
        }


        @Test
        fun `start scheduling`() {
            // given
            val workspace = mockk<Workspace>()
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
            val workspace = mockk<Workspace>()
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
            val workspace = mockk<Workspace>()
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
