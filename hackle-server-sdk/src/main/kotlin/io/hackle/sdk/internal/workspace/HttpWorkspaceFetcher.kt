package io.hackle.sdk.internal.workspace

import io.hackle.sdk.core.internal.metrics.Timer
import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.internal.http.body
import io.hackle.sdk.internal.http.isSuccessful
import io.hackle.sdk.internal.http.statusCode
import io.hackle.sdk.internal.monitoring.metrics.ApiCallMetrics
import io.hackle.sdk.internal.monitoring.metrics.ApiCallMetrics.GET_WORKSPACE
import io.hackle.sdk.internal.utils.parseJson
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import java.net.URI

/**
 * @author Yong
 */
internal class HttpWorkspaceFetcher(
    sdkBaseUrl: String,
    private val httpClient: CloseableHttpClient
) {

    private val sdkEndpoint = URI("$sdkBaseUrl/api/v2/workspaces")

    fun fetch(): Workspace {
        val sample = Timer.start()
        return try {
            fetchInternal()
                .also { ApiCallMetrics.record(GET_WORKSPACE, sample, true) }
        } catch (e: Throwable) {
            ApiCallMetrics.record(GET_WORKSPACE, sample, false)
            throw e
        }
    }

    private fun fetchInternal(): Workspace {
        return httpClient.execute(HttpGet(sdkEndpoint)).use { response ->
            check(response.isSuccessful) { "Http status code: ${response.statusCode}" }
            val body = response.body()
            val dto = body.parseJson<WorkspaceDto>()
            DefaultWorkspace.from(dto)
        }
    }
}
