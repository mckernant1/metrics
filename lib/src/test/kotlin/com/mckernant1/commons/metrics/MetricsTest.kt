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
            TestMetricsImpl("Test")
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
        val metrics = TestMetricsImpl("Test")
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
            "Test",
            setOf(Dimension("Host", "localhost"))
        )

        assertThrows<IllegalStateException> {
            metrics.newMetrics("Host" to "127.0.0.1")
        }

        assertThrows<IllegalStateException> {
            metrics.withDimensions("Host" to "127.0.1.1") {

            }
        }
    }

    @Test
    fun testWithMetrics() {
        val metrics = TestMetricsImpl("Test")

        metrics.withDimensions("Host" to "localhost") {
            val subMetrics = it as TestMetricsImpl
            assertTrue(subMetrics.exposeDimensions().isNotEmpty())
        }
    }

}
