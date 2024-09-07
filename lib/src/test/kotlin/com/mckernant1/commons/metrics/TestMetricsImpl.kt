package com.mckernant1.commons.metrics

class TestMetricsImpl(
    dimensions: Set<Dimension> = setOf()
) : Metrics(dimensions) {

    fun exposeMetrics(): List<Metric> {
        return metrics
    }

    fun exposeDimensions(): Set<Dimension> {
        return dimensions
    }

    override fun newMetricsInternal(dimensions: Set<Dimension>): Metrics {
        return TestMetricsImpl(dimensions)
    }

    override fun submitInternal() {

    }


}
