package com.mckernant1.commons.metrics

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DimensionTest {
    @Test
    fun testEquals() {
        val a = Dimension("D", "a")
        val b = Dimension("D", "b")

        assertEquals(a, b)


        assertNotEquals<Dimension?>(a, null)
    }

    @Test
    fun testHashCode() {
        val a = Dimension("D", "a")
        val b = Dimension("D", "b")

        assertEquals(a.hashCode(), b.hashCode())
    }

}
