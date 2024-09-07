package com.mckernant1.commons.metrics

data class Dimension(
    val name: String,
    val value: String
) {

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
