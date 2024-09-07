package com.mckernant1.commons.metrics.impls

import com.mckernant1.commons.logging.Slf4j.logger
import com.mckernant1.commons.metrics.Dimension
import com.mckernant1.commons.metrics.MetricUnit
import com.mckernant1.commons.metrics.MetricUnit.BYTES
import com.mckernant1.commons.metrics.MetricUnit.COUNT
import com.mckernant1.commons.metrics.MetricUnit.MILLISECONDS
import com.mckernant1.commons.metrics.MetricUnit.NONE
import com.mckernant1.commons.metrics.MetricUnit.PERCENT
import com.mckernant1.commons.metrics.MetricUnit.SECONDS
import com.mckernant1.commons.metrics.Metrics
import org.slf4j.Logger
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit
import software.amazon.awssdk.services.cloudwatch.model.Dimension as AwsDimension

class CloudWatchMetrics(
    private val namespace: String,
    private val cloudWatchClient: CloudWatchClient,
    dimensions: Set<Dimension> = setOf()
) : Metrics(dimensions) {

    companion object {
        fun MetricUnit.toStandardUnit(): StandardUnit = when (this) {
            COUNT -> StandardUnit.COUNT
            PERCENT -> StandardUnit.PERCENT
            SECONDS -> StandardUnit.SECONDS
            MILLISECONDS -> StandardUnit.MILLISECONDS
            BYTES -> StandardUnit.BYTES
            NONE -> StandardUnit.NONE
        }

        fun Dimension.toAwsDimension(): AwsDimension = AwsDimension.builder()
            .name(name)
            .value(value)
            .build()
    }

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
                .dimensions(dimensions.map { it.toAwsDimension() })
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
