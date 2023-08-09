package io.hackle.sdk

enum class Region(
    internal val sdkUrl: String,
    internal val eventUrl: String,
    internal val monitoringUrl: String,
) {

    DEFAULT(
        sdkUrl = "https://sdk.hackle.io",
        eventUrl = "https://event.hackle.io",
        monitoringUrl = "https://monitoring.hackle.io"
    ),

    STATIC(
        sdkUrl = "https://static-sdk.hackle.io",
        eventUrl = "https://static-event.hackle.io",
        monitoringUrl = "https://static-monitoring.hackle.io"
    )
}
