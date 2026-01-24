package com.mckernant1.commons.metrics

import com.mckernant1.commons.logging.Slf4j.logger
import com.mckernant1.commons.metrics.MetricUnit.Companion.toMetricUnit
import com.mckernant1.commons.standalone.measureOperation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger
import java.time.Duration
import java.util.LinkedList
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

/**
 * # Metrics object
 *
 * You should have one of these per project and use [Metrics.newMetrics] or
 * similar to create child metrics with different dimensions for different components.
 *
 * This metrics object is safe to share between threads. Though it is not recommended to submit metrics from multiple threads.
 *
 * This class has a locking mechanism.
 * 1. Only one metric may be added at a time
 * 2. Submitting metrics blocks the adding of new metrics
 *
 * @param dimensions are instantiated at creation.
 *
 */
abstract class Metrics(
    protected val dimensions: Set<Dimension>
) {

    private val metricsLock: Mutex = Mutex()
    private val logger: Logger = logger()

    protected val metrics: MutableList<Metric> = LinkedList()

    /**
     * Adds a count type metric
     */
    suspend fun addCount(name: String, value: Number) {
        addMetric(Metric(name, value, MetricUnit.COUNT))
    }

    /**
     * Adds a percentage type metric
     */
    suspend fun addPercentage(name: String, value: Double) {
        addMetric(Metric(name, value, MetricUnit.PERCENT))
    }

    /**
     * Adds a time type metric.
     *
     * @param unit Accepts Millis and Seconds
     */
    suspend fun addTime(name: String, duration: Duration, unit: TimeUnit = TimeUnit.MILLISECONDS) {
        addMetric(Metric(name, unit.convert(duration), unit.toMetricUnit()))
    }

    /**
     * Adds a metric to the metrics list.
     */
    suspend fun addMetric(metric: Metric) {
        metricsLock.withLock {
            metrics.add(metric)
        }
    }

    /**
     * Record the time of an operation and return a value
     */
    suspend fun <T> timeOperation(name: String, block: () -> T): T {
        val (time, result) = measureOperation(block)
        addTime(name, time)
        return result
    }

    /**
     * Run a block with a new metrics object and then submit
     */
    suspend fun <T> withNewMetrics(
        vararg dimensions: Pair<String, String>,
        block: (Metrics) -> T
    ): T {
        val localMetrics = newMetrics(*dimensions)
        val result = block(localMetrics)
        localMetrics.submitInternal()
        return result
    }

    /**
     * Runs a block with the current metrics and then submit.
     *
     * Not recommended for using with multithreaded
     */
    suspend fun <T> submitAndClear(block: suspend (Metrics) -> T): T {
        if (metrics.isNotEmpty()) {
            logger.warn("Metrics are not empty when entering submitAndClear block. Metrics added before the block will still be submitted")
        }
        val t = block(this)
        submitAndClear()
        return t
    }

    /**
     * This method should create a new [Metrics] with the list of dimensions
     *
     * @param dimensions this is sent in including the parent dimensions and possible additional dimensions based on the caller
     *
     * This should always return a child class instance with passed params
     */
    protected abstract fun newMetricsInternal(dimensions: Set<Dimension>): Metrics

    /**
     * Creates a new metrics object with new dimensions combined with existing dimensions
     *
     * @throws IllegalStateException if there is overlap between the existing dimensions and newly added dimensions
     */
    fun newMetrics(vararg dimensions: Pair<String, String>): Metrics {
        val newDimensions = dimensions
            .map { (name, value) -> Dimension(name, value) }

        if (newDimensions.any { this.dimensions.contains(it) }) {
            throw IllegalStateException("Attempting to add dimensions that already exist. currentDimensions: ${this.dimensions}, newDimensions: $newDimensions")
        }

        // Create a copy of current dimensions and add new dimensions
        val childDimensions = HashSet(this.dimensions)
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
     * WARNING: This will cause metric adds on any metrics object that is shared between threads to block until submit internal is finished
     */
    protected abstract suspend fun submitInternal()

    /**
     * Clears the current metrics
     */
    suspend fun clear() {
        metricsLock.withLock {
            metrics.clear()
        }
    }

    /**
     * Submits the metrics and clears the metrics list.
     *
     * Will block new metrics from being added until completed
     */
    suspend fun submitAndClear() {
        metricsLock.withLock {
            submitInternal()
            metrics.clear()
        }
    }
}
