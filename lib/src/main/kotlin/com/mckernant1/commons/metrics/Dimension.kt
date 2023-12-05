package com.mckernant1.commons.metrics

import software.amazon.awssdk.services.cloudwatch.model.Dimension as AwsDimension

data class Dimension(
    val name: String,
    val value: String
) {
    fun toDimension(): AwsDimension = AwsDimension.builder()
        .name(name)
        .value(value)
        .build()


    // Dimensions should be unique by name

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }

        if (other !is Dimension) {
            return false
        }

        return other.name == this.name
    }

    override fun hashCode(): Int {
        return this.name.hashCode()
    }
}
