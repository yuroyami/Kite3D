/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/SphericalHarmonics3.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.test.Test
import kotlin.test.assertTrue

class SphericalHarmonics3Test {

    @Test
    fun instancing() {
        val obj = SphericalHarmonics3()
        // three.js only asserts `assert.ok(object, ...)`. In Kotlin the constructor
        // cannot yield null; verify the invariant instead: 9 zeroed coefficients.
        assertTrue(obj.coefficients.size == 9)
        for (c in obj.coefficients) {
            assertTrue(c == Vector3())
        }
    }

    // No isSphericalHarmonics3 test: Kotlin checks the type with `is SphericalHarmonics3`.
}
