package com.mckernant1.commons.metrics

import java.util.concurrent.TimeUnit

enum class MetricUnit {
    COUNT,
    PERCENT,
    SECONDS,
    MILLISECONDS,
    BYTES,
    NONE
    ;

    companion object {
        fun TimeUnit.toMetricUnit(): MetricUnit = when (this) {
            TimeUnit.MILLISECONDS -> MILLISECONDS
            TimeUnit.SECONDS -> SECONDS
            else -> throw UnsupportedOperationException("Unsupported metric unit $this")
        }
    }

}
