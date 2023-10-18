package io.hackle.sdk.internal.workspace

import io.hackle.sdk.HackleConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.http.Header
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicHeader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

internal class HttpWorkspaceFetcherTest {

    @Test
    fun `when exception on http call then throws that exception`() {
        // given
        val httpClient = mockk<CloseableHttpClient>()
        every { httpClient.execute(any()) } answers { throw IOException("fail") }

        // when
        val sut = HttpWorkspaceFetcher(HackleConfig.DEFAULT, sdk("test_key"), httpClient)
        val actual = assertThrows<IOException> {
            sut.fetchIfModified()
        }

        // then
        expectThat(actual) {
            get { message } isEqualTo "fail"
        }
    }

    @Test
    fun `when workspace config is not modified then return null`() {
        // given
        val httpClient = mockk<CloseableHttpClient>()
        every { httpClient.execute(any()) } returns response(304, "")

        // when
        val sut = HttpWorkspaceFetcher(HackleConfig.DEFAULT, sdk("test_key"), httpClient)
        val actual = sut.fetchIfModified()

        // then
        expectThat(actual).isNull()
    }

    @Test
    fun `when http call is not success then throw exception`() {
        // given
        val httpClient = mockk<CloseableHttpClient>()
        every { httpClient.execute(any()) } returns response(500, "")

        // when
        val sut = HttpWorkspaceFetcher(HackleConfig.DEFAULT, sdk("test_key"), httpClient)
        val actual = assertThrows<IllegalStateException> {
            sut.fetchIfModified()
        }

        // then
        expectThat(actual) {
            get { message } isEqualTo "Http status code: 500"
        }
    }

    @Test
    fun `when successful to get then return workspace`() {
        // given
        val httpClient = mockk<CloseableHttpClient>()
        val json = String(Files.readAllBytes(Paths.get("src/test/resources/workspace_config.json")))
        every { httpClient.execute(any()) } returns response(200, json)

        // when
        val sut = HttpWorkspaceFetcher(HackleConfig.DEFAULT, sdk("test_key"), httpClient)
        val actual = sut.fetchIfModified()

        // then
        expectThat(actual).isNotNull().and {
            get { getExperimentOrNull(5) }.isNotNull()
        }
    }

    @Test
    fun `get url`() {
        val httpClient = mockk<CloseableHttpClient>()
        val json = String(Files.readAllBytes(Paths.get("src/test/resources/workspace_config.json")))
        every { httpClient.execute(any()) } returns response(200, json)

        val config = HackleConfig.builder()
            .sdkUrl("localhost")
            .build()

        // when
        val sut = HttpWorkspaceFetcher(config, sdk("SDK_KEY"), httpClient)
        val actual = sut.fetchIfModified()


        // then
        verify {
            httpClient.execute(withArg {
                expectThat(it) {
                    get { uri } isEqualTo URI("localhost/api/v2/workspaces/SDK_KEY/config")
                }
            })
        }
    }

    @Test
    fun `last modified`() {
        val httpClient = mockk<CloseableHttpClient>()
        val json = String(Files.readAllBytes(Paths.get("src/test/resources/workspace_config.json")))
        every { httpClient.execute(any()) } returnsMany listOf(
            response(200, json, BasicHeader("Last-Modified", "LAST_MODIFIED_HEADER_VALUE")),
            response(304, "")
        )
        val sut = HttpWorkspaceFetcher(HackleConfig.DEFAULT, sdk("SDK_KEY"), httpClient)

        expectThat(sut.fetchIfModified()).isNotNull()
        verify {
            httpClient.execute(match {
                it.getFirstHeader("If-Modified-Since") == null
            })
        }

        expectThat(sut.fetchIfModified()).isNull()
        verify {
            httpClient.execute(match {
                it.getFirstHeader("If-Modified-Since")?.value == "LAST_MODIFIED_HEADER_VALUE"
            })
        }
    }

    private fun response(code: Int, body: String, header: Header? = null): CloseableHttpResponse {
        return mockk(relaxed = true) {
            every { statusLine } returns mockk {
                every { statusCode } returns code
            }
            every { getFirstHeader(any()) } returns header
            every { entity } returns StringEntity(body, ContentType.APPLICATION_JSON)
        }
    }

    private fun sdk(key: String): Sdk {
        return Sdk(
            key = key,
            name = "test-sdk",
            version = "test-version",
        )
    }
}
