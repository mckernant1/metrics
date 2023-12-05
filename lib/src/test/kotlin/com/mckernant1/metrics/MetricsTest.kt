package com.mckernant1.metrics

import com.mckernant1.commons.standalone.delay
import com.mckernant1.commons.standalone.measureDuration
import com.mckernant1.commons.standalone.measureOperation
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
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

        assertEquals(3, metrics.exposeMetrics().size)
        assertEquals(6, metrics.exposeMetrics().filter { it.name == hello }.sumOf { it.value.toInt() })
    }

}
