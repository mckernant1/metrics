package com.mckernant1.commons.metrics.impls

import com.mckernant1.commons.standalone.measureOperation
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

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
