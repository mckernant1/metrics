package com.mckernant1.commons.metrics.impls

import com.mckernant1.commons.metrics.Dimension
import com.mckernant1.commons.metrics.Metric
import com.mckernant1.commons.metrics.Metrics
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

/**
 * This should take metric sending off the current path and will send through a channel to another thread where the submission work will take place.
 *
 * Probably better for I/O and writing to files
 */
class SendingChannelMetrics(
    private val sender: SendChannel<Collection<Metric>>,
    namespace: String,
    dimensions: Set<Dimension>
) : Metrics(namespace, dimensions) {

    override fun newMetricsInternal(dimensions: Set<Dimension>): Metrics {
        return SendingChannelMetrics(sender, namespace, dimensions)
    }

    /**
     * Sends a copy of the metrics list over the channel
     */
    override fun submitInternal() = runBlocking {
        sender.send(metrics.toList())
    }
}
