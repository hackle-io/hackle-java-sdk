package io.hackle.sdk

import io.hackle.sdk.core.HackleCore
import io.hackle.sdk.core.client
import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.scheduler.Schedulers
import io.hackle.sdk.core.internal.threads.NamedThreadFactory
import io.hackle.sdk.core.internal.threads.PoolingExecutors
import io.hackle.sdk.internal.client.HackleClientImpl
import io.hackle.sdk.internal.event.DefaultEventProcessor
import io.hackle.sdk.internal.event.EventDispatcher
import io.hackle.sdk.internal.http.SdkHeaderInterceptor
import io.hackle.sdk.internal.log.Slf4jLogger
import io.hackle.sdk.internal.workspace.HttpWorkspaceFetcher
import io.hackle.sdk.internal.workspace.PollingWorkspaceFetcher
import io.hackle.sdk.internal.workspace.Sdk
import io.hackle.sdk.internal.workspace.loadVersion
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.Executors.newSingleThreadScheduledExecutor

/**
 * @author Yong
 */
object HackleClients {

    @JvmStatic
    fun create(sdkKey: String): HackleClient {

        Logger.factory = Slf4jLogger.Factory

        val sdk = Sdk(
            key = sdkKey,
            name = "java-server-sdk",
            version = loadVersion()
        )

        val cm = PoolingHttpClientConnectionManager().apply {
            maxTotal = 20
            defaultMaxPerRoute = 20
        }

        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(5 * 1000)
            .setConnectionRequestTimeout(10 * 1000)
            .setSocketTimeout(10 * 1000)
            .build()

        val httpClient = HttpClients.custom()
            .setConnectionManager(cm)
            .setDefaultRequestConfig(requestConfig)
            .addInterceptorLast(SdkHeaderInterceptor(sdk))
            .build()

        val httpWorkspaceFetcher = HttpWorkspaceFetcher(
            sdkBaseUrl = "https://sdk.hackle.io",
            httpClient = httpClient
        )

        val pollingWorkspaceFetcher = PollingWorkspaceFetcher(
            httpWorkspaceFetcher = httpWorkspaceFetcher,
            pollingIntervalMillis = 10 * 1000,
            scheduler = Schedulers.executor(
                newSingleThreadScheduledExecutor(NamedThreadFactory("Hackle-WorkspacePolling-", true))
            )
        )

        val eventDispatcher = EventDispatcher(
            eventBaseUrl = "https://event.hackle.io",
            httpClient = httpClient,
            dispatcherExecutor = PoolingExecutors.newThreadPool(
                poolSize = 4,
                workQueueCapacity = 100,
                threadFactory = NamedThreadFactory("Hackle-EventDispatcher-", true)
            ),
            shutdownTimeoutMillis = 10 * 1000
        )

        val defaultEventProcessor = DefaultEventProcessor(
            queue = ArrayBlockingQueue(10000),
            eventDispatcher = eventDispatcher,
            eventDispatchSize = 500,
            flushScheduler = Schedulers.executor(
                newSingleThreadScheduledExecutor(NamedThreadFactory("Hackle-EventFlush-", true))
            ),
            flushIntervalMillis = 10 * 1000,
            consumingExecutor = Executors.newSingleThreadExecutor(NamedThreadFactory("Hackle-EventConsumer-", true)),
            shutdownTimeoutMillis = 10 * 1000
        )

        val internalClient = HackleCore.client(
            workspaceFetcher = pollingWorkspaceFetcher.apply { start() },
            eventProcessor = defaultEventProcessor.apply { start() }
        )

        return HackleClientImpl(client = internalClient)
    }
}
