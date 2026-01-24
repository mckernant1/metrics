package com.mckernant1.commons.metrics.guava

import com.google.common.cache.CacheStats
import com.mckernant1.commons.metrics.Metric
import com.mckernant1.commons.metrics.MetricUnit
import com.mckernant1.commons.metrics.Metrics

/**
 * extensions for adding LoadingCache stats to [Metrics]
 */
object CacheMetrics {
    /**
     * Adds Cache stats to metrics object.
     *
     * Note: This method does not add a dimension for the cache name
     */
    suspend fun Metrics.addCacheStats(cacheStats: CacheStats) {
        this.addMetrics(
            listOf(
                Metric(name = "hitCount", value = cacheStats.hitCount(), unit = MetricUnit.COUNT),
                Metric(name = "evictionCount", value = cacheStats.evictionCount(), unit = MetricUnit.COUNT),
                Metric(name = "loadCount", value = cacheStats.loadCount(), unit = MetricUnit.COUNT),
                Metric(name = "missCount", value = cacheStats.missCount(), unit = MetricUnit.COUNT),
                Metric(name = "loadExceptionCount", value = cacheStats.loadExceptionCount(), unit = MetricUnit.COUNT),
                Metric(name = "loadSuccessCount", value = cacheStats.loadSuccessCount(), unit = MetricUnit.COUNT),
                Metric(name = "missRate", value = cacheStats.missRate() * 100, unit = MetricUnit.PERCENT),
                Metric(name = "loadExceptionRate", value = cacheStats.loadExceptionRate() * 100, unit = MetricUnit.PERCENT),
                Metric(name = "hitRate", value = cacheStats.hitRate() * 100, unit = MetricUnit.PERCENT),
                Metric(name = "averageLoadPenalty", value = cacheStats.averageLoadPenalty(), unit = MetricUnit.MILLISECONDS),
                Metric(name = "totalLoadTime", value = cacheStats.totalLoadTime(), unit = MetricUnit.MILLISECONDS),
                Metric(name = "requestCount", value = cacheStats.requestCount(), unit = MetricUnit.COUNT)
            )
        )
    }
}
