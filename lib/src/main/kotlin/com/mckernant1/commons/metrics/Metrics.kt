package com.mckernant1.commons.metrics

import com.mckernant1.commons.standalone.measureOperation
import java.time.Duration
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

/**
 * # Metrics object.
 *
 * You should have one of these per project and use getMetrics to create child metrics
 * with different dimensions.
 *
 * There should be one overarching metrics object per project and then other newMetrics are branched off for components
 *
 * This metrics object is safe to share between threads. Though it is not recommended to submit metrics from multiple threads
 *
 * Metrics list will be added to and will submit and clear simultaneously in a locking fashion.
 *
 * @param namespace The namespace of the metric. Usually service differentiator
 * @param dimensions are instantiated at creation. dimensions are attached to metrics as a String String map
 *
 */
abstract class Metrics(
    protected val namespace: String,
    protected val dimensions: Set<Dimension>
) {

    private val metricsLock: ReentrantLock = ReentrantLock()
    protected val metrics: MutableList<Metric> = mutableListOf()

    /**
     * Adds a count type metric
     */
    fun addCount(name: String, value: Number) {
        addMetric(Metric(name, value, MetricUnit.COUNT))
    }

    /**
     * Adds a percentage type metric
     */
    fun addPercentage(name: String, value: Double) {
        addMetric(Metric(name, value, MetricUnit.PERCENT))
    }

    /**
     * Adds a time type metric in millis
     */
    fun addTime(name: String, duration: Duration) {
        addMetric(Metric(name, duration.toMillis(), MetricUnit.MILLISECONDS))
    }

    /**
     * Adds a metric to the metrics list. This
     */
    fun addMetric(metric: Metric) {
        metricsLock.withLock {
            metrics.add(metric)
        }
    }

    /**
     * Record the time of an operation and return a value
     */
    fun <T> timeOperation(name: String, block: () -> T): T {
        val (time, result) = measureOperation(block)
        addTime(name, time)
        return result
    }

    /**
     * Run a block within the scope of a new dimensions and then submit
     */
    fun <T> withDimensions(
        vararg dimensions: Pair<String, String>,
        block: (Metrics) -> T
    ): T {
        val localMetrics = newMetrics(*dimensions)
        val result = block(localMetrics)
        localMetrics.submitInternal()
        return result
    }

    /**
     * This method should create a new [Metrics] with the list of dimensions
     *
     * @param dimensions this is sent in including the parent dimensions and possible additional dimensions based on the caller
     *
     * This should always return a child class instance with passed params
     */
    protected abstract fun newMetricsInternal(dimensions: Set<Dimension>): Metrics

    fun newMetrics(vararg dimensions: Pair<String, String>): Metrics {
        val newDimensions = dimensions
            .map { (name, value) -> Dimension(name, value) }

        if (newDimensions.intersect(this.dimensions).isNotEmpty()) {
            throw IllegalStateException("Attempting to add Dimensions that already exist. currentDimensions: ${this.dimensions}, newDimensions: $newDimensions")
        }

        // Create a copy of current dimensions and add new dimensions
        val childDimensions = this.dimensions.toMutableSet()
        childDimensions.addAll(newDimensions)

        return newMetricsInternal(childDimensions)
    }


    fun newMetricsForClass(clazz: KClass<out Any>): Metrics = newMetrics(
        MetricsConstants.CLASS_NAME to clazz.simpleName!!
    )


    inline fun <reified T> newMetricsForClass(): Metrics = newMetrics(
        MetricsConstants.CLASS_NAME to T::class.simpleName!!
    )

    /**
     * submits the metrics to whatever source.
     *
     * Metrics have already been locked when this method is called so no new metrics may be submitted.
     *
     * WARNING: This will cause metric adds on the hot path to block until submit internal is finished
     */
    protected abstract fun submitInternal()


    /**
     * Submits the metrics and clears the metrics list.
     *
     * Will block new metrics from being added until completed
     */
    fun submitAndClear() {
        metricsLock.withLock {
            submitInternal()
            metrics.clear()
        }
    }
}
