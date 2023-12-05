package com.mckernant1.commons.metrics

/**
 * Marks a class that will periodic submit metrics
 */
interface PeriodicSubmitMetrics {

    val metrics: List<Metrics>

}
