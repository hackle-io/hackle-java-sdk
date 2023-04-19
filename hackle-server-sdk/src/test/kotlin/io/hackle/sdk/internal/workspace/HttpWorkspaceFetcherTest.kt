package io.hackle.sdk.internal.workspace

import io.mockk.every
import io.mockk.mockk
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.nio.file.Files
import java.nio.file.Paths

internal class HttpWorkspaceFetcherTest {

    @Test
    fun `get fail`() {

        val httpClient = mockk<CloseableHttpClient>()
        every { httpClient.execute(any()) } returns response(500, "")

        val sut = HttpWorkspaceFetcher("localhost", httpClient)


        val exception = assertThrows<IllegalStateException> {
            sut.fetch()
        }

        expectThat(exception.message) isEqualTo "Http status code: 500"
    }

    @Test
    fun `get workspace`() {
        val responseBody = String(Files.readAllBytes(Paths.get("src/test/resources/workspace_config.json")))
        val httpClient = mockk<CloseableHttpClient>()
        every { httpClient.execute(any()) } returns response(200, responseBody)

        val sut = HttpWorkspaceFetcher("localhost", httpClient)

        val workspace = sut.fetch()

        expectThat(workspace.getExperimentOrNull(5)).isNotNull()
    }

    private fun response(code: Int, body: String): CloseableHttpResponse {
        return mockk(relaxed = true) {
            every { statusLine } returns mockk {
                every { statusCode } returns code
            }
            every { entity } returns StringEntity(body, "application/json", "utf-8")
        }
    }
}
