package com.mckernant1.commons.metrics

import com.mckernant1.commons.standalone.delay
import com.mckernant1.commons.standalone.measureDuration
import com.mckernant1.commons.standalone.measureOperation
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MetricsTest {

    @Test
    fun testTime(): Unit = runBlocking {
        val (time, metrics) = measureOperation {
            TestMetricsImpl()
        }
        metrics.addTime("TimeToCreateMetrics", time)

        val duration = measureDuration {
            delay(Duration.ofMillis(100))
        }
        metrics.addTime("Over100Millis", duration)

        assertNotNull(metrics.exposeMetrics().find {
            it.name == "TimeToCreateMetrics"
        })

        val timeDelayMetric = metrics.exposeMetrics().find {
            it.name == "Over100Millis"
        }!!

        assertTrue(timeDelayMetric.value.toInt() > 100, "${timeDelayMetric.value.toInt()} was supposed to be over 100")
    }

    @Test
    fun testCount() = runBlocking {
        val metrics = TestMetricsImpl()
        val hello = "Hello"
        val hello1 = "Hello1"
        metrics.addCount(hello, 5)
        metrics.addCount(hello, 1)

        metrics.addCount(hello1, 10)

        val metricsByName = metrics.exposeMetrics()
            .groupBy { it.name }

        assertEquals(3, metrics.exposeMetrics().size)
        assertEquals(2, metricsByName.size)
        assertEquals(6, metricsByName[hello]?.sumOf { it.value.toInt() })
    }

    @Test
    fun throwOnDuplicateDimensions() {
        val metrics = TestMetricsImpl(
            setOf(Dimension("Host", "localhost"))
        )

        metrics.newMetrics("Hostname" to "google.com")

        assertThrows<IllegalStateException> {
            metrics.newMetrics("Host" to "127.0.0.1")
        }

        assertThrows<IllegalStateException> {
            metrics.withNewMetrics("Host" to "127.0.1.1") {}
        }
    }

    @Test
    fun testWithNewMetrics() {
        val metrics = TestMetricsImpl()

        metrics.withNewMetrics("Host" to "localhost") {
            val subMetrics = it as TestMetricsImpl
            assertTrue(subMetrics.exposeDimensions().isNotEmpty())
        }
    }

    @Test
    fun testSubmitAndClear() {
        val metrics = TestMetricsImpl()

        metrics.submitAndClear {
            val subMetrics = it as TestMetricsImpl
            subMetrics.addCount("test", 1)
            assertTrue(subMetrics.exposeMetrics().isNotEmpty())
            assertTrue(subMetrics.exposeDimensions().isEmpty())
        }
        assertTrue(metrics.exposeMetrics().isEmpty())
    }

    @Test
    fun testClear() {
        val metrics = TestMetricsImpl()

        metrics.addCount("test", 1)
        assertTrue(metrics.exposeMetrics().isNotEmpty())

        metrics.clear()
        assertTrue(metrics.exposeMetrics().isEmpty())
    }

}
