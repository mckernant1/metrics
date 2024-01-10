package com.mckernant1.commons.metrics

import software.amazon.awssdk.services.cloudwatch.model.StandardUnit

enum class MetricUnit {
    COUNT,
    PERCENT,
    SECONDS,
    MILLISECONDS,
    BYTES
    ;

    fun toStandardUnit(): StandardUnit = when (this) {
        COUNT -> StandardUnit.COUNT
        PERCENT -> StandardUnit.PERCENT
        SECONDS -> StandardUnit.SECONDS
        MILLISECONDS -> StandardUnit.MILLISECONDS
        BYTES -> StandardUnit.BYTES
    }

}
