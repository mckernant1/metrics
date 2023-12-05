package com.mckernant1.commons.metrics

import software.amazon.awssdk.services.cloudwatch.model.Dimension

data class Dimension(
    val name: String,
    val value: String
) {
    fun toDimension(): Dimension = Dimension.builder()
        .name(name)
        .value(value)
        .build()
}
