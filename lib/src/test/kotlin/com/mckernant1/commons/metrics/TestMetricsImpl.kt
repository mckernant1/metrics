package com.mckernant1.commons.metrics

class TestMetricsImpl(
    namespace: String,
    dimensions: Set<Dimension> = setOf()
) : Metrics(namespace, dimensions) {

    fun exposeMetrics(): List<Metric> {
        return metrics
    }

    fun exposeDimensions(): Set<Dimension> {
        return dimensions
    }

    override fun newMetricsInternal(dimensions: Set<Dimension>): Metrics {
        return TestMetricsImpl(namespace, dimensions)
    }

    override fun submitInternal() {

    }


}
