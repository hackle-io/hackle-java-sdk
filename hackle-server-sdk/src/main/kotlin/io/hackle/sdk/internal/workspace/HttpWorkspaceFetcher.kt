package io.hackle.sdk.internal.workspace

import io.hackle.sdk.HackleConfig
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.internal.http.body
import io.hackle.sdk.internal.http.isNotModified
import io.hackle.sdk.internal.http.isSuccessful
import io.hackle.sdk.internal.http.statusCode
import io.hackle.sdk.internal.monitoring.metrics.ApiCallMetrics
import io.hackle.sdk.internal.monitoring.metrics.ApiCallMetrics.GET_WORKSPACE
import io.hackle.sdk.internal.utils.parseJson
import org.apache.http.HttpHeaders.IF_MODIFIED_SINCE
import org.apache.http.HttpHeaders.LAST_MODIFIED
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.CloseableHttpClient
import java.net.URI

/**
 * @author Yong
 */
internal class HttpWorkspaceFetcher(
    config: HackleConfig,
    sdk: Sdk,
    private val httpClient: CloseableHttpClient
) {

    private val url = URI(url(config, sdk))
    private var lastModified: String? = null

    fun fetchIfModified(): Workspace? {
        val request = createRequest()
        val response = execute(request)
        return response.use { handleResponse(it) }
    }

    private fun createRequest(): HttpUriRequest {
        val request = HttpGet(url)
        lastModified?.let { request.addHeader(IF_MODIFIED_SINCE, it) }
        return request
    }

    private fun execute(request: HttpUriRequest): CloseableHttpResponse {
        return ApiCallMetrics.record(GET_WORKSPACE) {
            httpClient.execute(request)
        }
    }

    private fun handleResponse(response: CloseableHttpResponse): Workspace? {
        if (response.isNotModified) {
            return null
        }
        check(response.isSuccessful) { "Http status code: ${response.statusCode}" }
        lastModified = response.getFirstHeader(LAST_MODIFIED)?.value
        val body = response.body()
        val dto = body.parseJson<WorkspaceDto>()
        return DefaultWorkspace.from(dto)
    }

    companion object {
        private fun url(config: HackleConfig, sdk: Sdk): String {
            return "${config.sdkUrl}/api/v2/workspaces/${sdk.key}/config"
        }
    }
}
