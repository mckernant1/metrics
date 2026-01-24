package com.mckernant1.commons.metrics.guava

import com.google.common.cache.CacheStats
import com.mckernant1.commons.metrics.Metrics
import java.time.Duration

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
        this.addCount("hitCount", cacheStats.hitCount())
        this.addCount("evictionCount", cacheStats.evictionCount())
        this.addCount("loadCount", cacheStats.loadCount())
        this.addCount("missCount", cacheStats.missCount())

        this.addCount("loadExceptionCount", cacheStats.loadExceptionCount())
        this.addCount("loadSuccessCount", cacheStats.loadSuccessCount())

        this.addPercentage("missRate", cacheStats.missRate() * 100)
        this.addPercentage("loadExceptionRate", cacheStats.loadExceptionRate() * 100)
        this.addPercentage("hitRate", cacheStats.hitRate() * 100)

        this.addTime("averageLoadPenalty", Duration.ofNanos(cacheStats.averageLoadPenalty().toLong()))

        this.addTime("totalLoadTime", Duration.ofNanos(cacheStats.totalLoadTime()))
        this.addCount("requestCount", cacheStats.requestCount())
    }

}
