package io.hackle.sdk.internal.http

import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.internal.workspace.Sdk
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.protocol.HttpContext

/**
 * @author Yong
 */
internal class SdkHeaderInterceptor(
    private val sdk: Sdk,
    private val clock: Clock = Clock.SYSTEM
) : HttpRequestInterceptor {

    override fun process(request: HttpRequest, context: HttpContext) {
        request.addHeader(SDK_KEY_HEADER, sdk.key)
        request.addHeader(SDK_NAME_HEADER, sdk.name)
        request.addHeader(SDK_VERSION_HEADER, sdk.version)
        request.addHeader(SDK_TIME_HEADER_NAME, clock.currentMillis().toString())
        request.addHeader(USER_AGENT, "${sdk.name}/${sdk.version}")
    }

    companion object {
        private const val SDK_KEY_HEADER = "X-HACKLE-SDK-KEY"
        private const val SDK_NAME_HEADER = "X-HACKLE-SDK-NAME"
        private const val SDK_VERSION_HEADER = "X-HACKLE-SDK-VERSION"
        private const val SDK_TIME_HEADER_NAME = "X-HACKLE-SDK-TIME"
        private const val USER_AGENT = "User-Agent"
    }
}
