package io.hackle.sdk.internal.http

import org.apache.http.HttpResponse
import org.apache.http.util.EntityUtils
import java.net.HttpURLConnection.HTTP_NOT_MODIFIED

private val SUCCESSFUL_CODE_RANGE = 200..299
internal val HttpResponse.statusCode: Int get() = statusLine.statusCode
internal val HttpResponse.isSuccessful: Boolean get() = statusCode in SUCCESSFUL_CODE_RANGE
internal val HttpResponse.isNotModified: Boolean get() = statusCode == HTTP_NOT_MODIFIED
internal fun HttpResponse.body(): String = EntityUtils.toString(entity, "UTF-8")
