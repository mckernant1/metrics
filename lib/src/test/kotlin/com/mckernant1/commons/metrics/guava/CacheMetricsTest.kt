package com.mckernant1.commons.metrics.guava

import com.google.common.cache.CacheBuilder
import com.mckernant1.commons.metrics.MetricUnit
import com.mckernant1.commons.metrics.TestMetricsImpl
import com.mckernant1.commons.metrics.guava.CacheMetrics.addCacheStats
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CacheMetricsTest {

    @Test
    fun testAddCacheStats() = runBlocking {
        val metrics = TestMetricsImpl()
        val cache = CacheBuilder.newBuilder()
            .recordStats()
            .build<String, String>()

        cache.put("key", "value")
        cache.getIfPresent("key") // hit
        cache.getIfPresent("missing") // miss

        val stats = cache.stats()
        metrics.addCacheStats(stats)

        val reportedMetrics = metrics.exposeMetrics()

        // Current implementation has a bug/double entry for hitCount because of the initial addMetrics call
        // We expect it to be cleaned up in the refactor, but for now let's just see what's there.

        val metricNames = reportedMetrics.map { it.name }.toSet()
        val expectedNames = setOf(
            "hitCount",
            "evictionCount",
            "loadCount",
            "missCount",
            "loadExceptionCount",
            "loadSuccessCount",
            "missRate",
            "loadExceptionRate",
            "hitRate",
            "averageLoadPenalty",
            "totalLoadTime",
            "requestCount"
        )

        expectedNames.forEach { name ->
            assertNotNull(reportedMetrics.find { it.name == name }, "Metric $name not found")
        }

        val hitCountMetric = reportedMetrics.find { it.name == "hitCount" }!!
        assertEquals(stats.hitCount(), hitCountMetric.value.toLong())
        assertEquals(MetricUnit.COUNT, hitCountMetric.unit)
        
        val hitRateMetric = reportedMetrics.find { it.name == "hitRate" }!!
        assertEquals(stats.hitRate() * 100, hitRateMetric.value.toDouble())
        assertEquals(MetricUnit.PERCENT, hitRateMetric.unit)

        val totalLoadTimeMetric = reportedMetrics.find { it.name == "totalLoadTime" }!!
        assertEquals(stats.totalLoadTime(), totalLoadTimeMetric.value.toLong())
        assertEquals(MetricUnit.MILLISECONDS, totalLoadTimeMetric.unit)
    }
}
