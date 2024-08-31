package com.mckernant1.commons.metrics.impls

import com.mckernant1.commons.logging.Slf4j.logger
import com.mckernant1.commons.metrics.Dimension
import com.mckernant1.commons.metrics.Metrics
import org.slf4j.Logger
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum

class CloudWatchMetrics(
    namespace: String,
    private val cloudWatchClient: CloudWatchClient,
    dimensions: Set<Dimension> = setOf()
) : Metrics(namespace, dimensions) {

    private val logger: Logger = logger()

    override fun newMetricsInternal(dimensions: Set<Dimension>): Metrics {
        return CloudWatchMetrics(namespace, cloudWatchClient, dimensions)
    }

    override fun submitInternal() {
        logger.debug("Submitting metrics")
        val datum = metrics.map { metric ->
            MetricDatum.builder()
                .metricName(metric.name)
                .timestamp(metric.timestamp)
                .value(metric.value.toDouble())
                .unit(metric.unit.toStandardUnit())
                .dimensions(dimensions.map(Dimension::toDimension))
                .build()
        }

        if (datum.size > 1000) {
            logger.warn("We are sending over 1000 metrics from a single metrics object!")
        }

        for (metricDatas in datum.chunked(1000)) {
            cloudWatchClient.putMetricData {
                it.namespace(namespace)
                it.metricData(metricDatas)
            }
        }
    }


}
