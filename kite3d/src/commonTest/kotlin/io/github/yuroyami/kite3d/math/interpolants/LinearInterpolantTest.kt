/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/interpolants/LinearInterpolant.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math.interpolants

import io.github.yuroyami.kite3d.math.Interpolant
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LinearInterpolantTest {

    // JS passes `null` for parameterPositions; the pinned Kotlin ctor is
    // non-nullable, so pass an empty array (never read by these ctor-only tests).

    // INHERITANCE
    @Test
    fun extending() {
        val obj = LinearInterpolant(DoubleArray(0), doubleArrayOf(1.0, 11.0, 2.0, 22.0, 3.0, 33.0), 2, DoubleArray(0))
        assertTrue(obj is Interpolant, "LinearInterpolant extends from Interpolant")
    }

    // INSTANCING
    @Test
    fun instancing() {
        // parameterPositions, sampleValues, sampleSize, resultBuffer
        val obj = LinearInterpolant(DoubleArray(0), doubleArrayOf(1.0, 11.0, 2.0, 22.0, 3.0, 33.0), 2, DoubleArray(0))
        assertNotNull(obj, "Can instantiate a LinearInterpolant.")
    }
}
