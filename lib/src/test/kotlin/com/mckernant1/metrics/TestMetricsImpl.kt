package com.mckernant1.metrics

import com.mckernant1.commons.metrics.Dimension
import com.mckernant1.commons.metrics.Metric
import com.mckernant1.commons.metrics.Metrics

class TestMetricsImpl(
    namespace: String,
    dimensions: List<Dimension> = listOf()
) : Metrics(namespace, dimensions) {

    fun exposeMetrics(): List<Metric> {
        return metrics
    }

    fun exposeDimensions(): List<Dimension> {
        return dimensions
    }

    override fun newMetricsInternal(dimensions: List<Dimension>): Metrics {
        return TestMetricsImpl(namespace, dimensions)
    }

    override fun submitInternal() {

    }


}
