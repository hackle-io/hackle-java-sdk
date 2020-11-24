package io.hackle.sdk.internal.workspace

import io.hackle.sdk.core.workspace.Workspace
import io.hackle.sdk.internal.http.body
import io.hackle.sdk.internal.http.isSuccessful
import io.hackle.sdk.internal.http.statusCode
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

    private val sdkEndpoint = URI("$sdkBaseUrl/api/v1/workspaces")

    fun fetch(): Workspace {
        return httpClient.execute(HttpGet(sdkEndpoint)).use { response ->
            check(response.isSuccessful) { "Http status code: ${response.statusCode}" }
            val body = response.body()
            val dto = body.parseJson<WorkspaceDto>()
            WorkspaceImpl.from(dto)
        }
    }
}
