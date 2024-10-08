package com.mckernant1.commons.metrics.impls

import com.fasterxml.jackson.databind.ObjectMapper
import com.mckernant1.commons.logging.Slf4j.logger
import com.mckernant1.commons.metrics.Dimension
import com.mckernant1.commons.metrics.Metrics

/**
 *
 */
class NoopMetrics(
    dimensions: Set<Dimension> = setOf(),
    private val mapper: ObjectMapper = ObjectMapper()
) : Metrics(dimensions) {

    private val logger = logger()

    override fun newMetricsInternal(dimensions: Set<Dimension>): Metrics {
        return NoopMetrics(dimensions, mapper)
    }

    override fun submitInternal() {
        logger.debug("Submitting metrics with dimensions {}", dimensions)
        for (metric in metrics) {
            logger.debug("Publishing metric {}", mapper.writeValueAsString(metric))
        }
    }

}
