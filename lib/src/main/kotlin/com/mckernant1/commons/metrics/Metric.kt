package com.mckernant1.commons.metrics

import java.time.Instant

data class Metric(
    val name: String,
    val value: Number,
    val unit: MetricUnit,
    val timestamp: Instant = Instant.now()
)
