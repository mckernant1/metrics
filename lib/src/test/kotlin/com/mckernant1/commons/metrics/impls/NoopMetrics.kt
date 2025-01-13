package com.mckernant1.commons.metrics.impls

import com.mckernant1.commons.metrics.Dimension
import com.mckernant1.commons.standalone.delay
import com.mckernant1.commons.standalone.measureDuration
import com.mckernant1.commons.standalone.measureOperation
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

class MetricsTest {

    @Test
    fun testTypes(): Unit = runBlocking {
        val (time, metrics) = measureOperation {
            NoopMetrics()
        }
        metrics.addTime("TimeToCreateMetrics", time)
        metrics.addCount("TestCount", 1)
        metrics.addPercentage("Over100Percent", 100.0)

        metrics.submitAndClear()
    }



}
