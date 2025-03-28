package io.hackle.sdk

import io.hackle.sdk.core.HackleCore
import io.hackle.sdk.core.evaluation.EvaluationContext
import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.log.metrics.MetricLoggerFactory
import io.hackle.sdk.core.internal.metrics.Metrics
import io.hackle.sdk.core.internal.scheduler.Schedulers
import io.hackle.sdk.core.internal.threads.NamedThreadFactory
import io.hackle.sdk.core.internal.threads.PoolingExecutors
import io.hackle.sdk.internal.client.HackleClientImpl
import io.hackle.sdk.internal.event.DefaultEventProcessor
import io.hackle.sdk.internal.event.EventDispatcher
import io.hackle.sdk.internal.http.SdkHeaderInterceptor
import io.hackle.sdk.internal.log.Slf4jLogger
import io.hackle.sdk.internal.monitoring.metrics.MonitoringMetricRegistry
import io.hackle.sdk.internal.user.HackleUserResolver
import io.hackle.sdk.internal.workspace.HttpWorkspaceFetcher
import io.hackle.sdk.internal.workspace.PollingWorkspaceFetcher
import io.hackle.sdk.internal.workspace.Sdk
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.Executors.newSingleThreadScheduledExecutor
import java.util.concurrent.TimeUnit

/**
 * @author Yong
 */
object HackleClients {

    private val CLIENTS = hashMapOf<String, HackleClient>()

    @JvmOverloads
    @JvmStatic
    fun create(sdkKey: String, config: HackleConfig = HackleConfig.DEFAULT): HackleClient {
        return synchronized(this) {
            // Do NOT use computeIfAbsent to support below JDK 1.8
            CLIENTS.getOrPut(sdkKey) { createHackleClient(sdkKey, config) }
        }
    }

    private fun createHackleClient(sdkKey: String, config: HackleConfig): HackleClient {
        loggerConfiguration()

        val sdk = Sdk.load(sdkKey)
        val httpClient = httpClient(sdk)

        metricConfiguration(config, httpClient)

        val httpWorkspaceFetcher = HttpWorkspaceFetcher(
            config = config,
            sdk = sdk,
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
            config = config,
            httpClient = httpClient,
            dispatcherExecutor = PoolingExecutors.newThreadPool(
                poolSize = 4,
                workQueueCapacity = 10000,
                threadFactory = NamedThreadFactory("Hackle-EventDispatcher-", true)
            ),
            shutdownTimeoutMillis = 10 * 1000
        )

        val defaultEventProcessor = DefaultEventProcessor(
            queue = ArrayBlockingQueue(10000),
            eventDispatcher = eventDispatcher,
            eventDispatchSize = 100,
            flushScheduler = Schedulers.executor(
                newSingleThreadScheduledExecutor(NamedThreadFactory("Hackle-EventFlush-", true))
            ),
            flushIntervalMillis = 10 * 1000,
            consumingExecutor = Executors.newSingleThreadExecutor(NamedThreadFactory("Hackle-EventConsumer-", true)),
            shutdownTimeoutMillis = 10 * 1000
        )

        val core = HackleCore.create(
            context = EvaluationContext.GLOBAL,
            workspaceFetcher = pollingWorkspaceFetcher.apply { start() },
            eventProcessor = defaultEventProcessor.apply { start() }
        )

        return HackleClientImpl(
            core = core,
            userResolver = HackleUserResolver()
        )
    }

    private fun httpClient(sdk: Sdk): CloseableHttpClient {
        val cm = PoolingHttpClientConnectionManager(10, TimeUnit.MINUTES).apply {
            maxTotal = 20
            defaultMaxPerRoute = 20
            validateAfterInactivity = 2000
        }

        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(5 * 1000)
            .setConnectionRequestTimeout(10 * 1000)
            .setSocketTimeout(10 * 1000)
            .build()

        return HttpClients.custom()
            .evictIdleConnections(30, TimeUnit.SECONDS)
            .setConnectionManager(cm)
            .setDefaultRequestConfig(requestConfig)
            .addInterceptorLast(SdkHeaderInterceptor(sdk))
            .disableCookieManagement()
            .build()
    }

    private fun loggerConfiguration() {
        Logger.add(Slf4jLogger.Factory)
        Logger.add(MetricLoggerFactory(Metrics.globalRegistry))
    }

    private fun metricConfiguration(config: HackleConfig, httpClient: CloseableHttpClient) {
        val registry = MonitoringMetricRegistry(
            monitoringBaseUrl = config.monitoringUrl,
            scheduler = Schedulers.executor("MonitoringMetricRegistry-"),
            flushIntervalMillis = 60 * 1000,
            httpClient = httpClient
        )
        Metrics.addRegistry(registry)
    }
}
