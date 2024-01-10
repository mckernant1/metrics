package com.mckernant1.commons.metrics.impls

import com.mckernant1.commons.metrics.Metric
import com.mckernant1.commons.metrics.Metrics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class SendingChannelMetricsTest {

    @Test
    fun testChannelMetrics(): Unit = runBlocking {
        val chan = Channel<Collection<Metric>>()
        val metrics: Metrics = SendingChannelMetrics(chan, "TEST", setOf())

        val j = launch {
            metrics.addCount("Test1", 5)

            metrics.submitAndClear()

            metrics.addCount("Test2", 5)

            metrics.submitAndClear()
            chan.close()
        }

        val l = mutableListOf<Metric>()
        chan.consumeEach {
            l.addAll(it)
        }
        assertEquals(2, l.size)
        j.join()
    }

}
