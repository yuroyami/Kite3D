/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Cylindrical.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CylindricalTest {

    @Test
    fun instancing() {
        var a = Cylindrical()
        val radius = 10.0
        val theta = PI
        val y = 5.0

        assertEquals(1.0, a.radius, 0.0, "Default values: check radius")
        assertEquals(0.0, a.theta, 0.0, "Default values: check theta")
        assertEquals(0.0, a.y, 0.0, "Default values: check y")

        a = Cylindrical(radius, theta, y)
        assertEquals(radius, a.radius, 0.0, "Custom values: check radius")
        assertEquals(theta, a.theta, 0.0, "Custom values: check theta")
        assertEquals(y, a.y, 0.0, "Custom values: check y")
    }

    @Test
    fun set() {
        val a = Cylindrical()
        val radius = 10.0
        val theta = PI
        val y = 5.0

        a.set(radius, theta, y)
        assertEquals(radius, a.radius, 0.0, "Check radius")
        assertEquals(theta, a.theta, 0.0, "Check theta")
        assertEquals(y, a.y, 0.0, "Check y")
    }

    @Test
    fun clone() {
        val radius = 10.0
        val theta = PI
        val y = 5.0
        val a = Cylindrical(radius, theta, y)
        val b = a.clone()

        // three.js uses propEqual (structural, all fields) → Kotlin structural `==`.
        assertTrue(a == b, "Check a and b are equal after clone()")

        a.radius = 1.0
        assertFalse(a == b, "Check a and b are not equal after modification")
    }

    @Test
    fun copy() {
        val radius = 10.0
        val theta = PI
        val y = 5.0
        val a = Cylindrical(radius, theta, y)
        val b = Cylindrical().copy(a)

        assertTrue(a == b, "Check a and b are equal after copy()")

        a.radius = 1.0
        assertFalse(a == b, "Check a and b are not equal after modification")
    }

    @Test
    fun setFromVector3() {
        val a = Cylindrical(1.0, 1.0, 1.0)
        val b = Vector3(0.0, 0.0, 0.0)
        val c = Vector3(3.0, -1.0, -3.0)
        val expected = Cylindrical(sqrt(9.0 + 9.0), atan2(3.0, -3.0), -1.0)

        a.setFromVector3(b)
        assertEquals(0.0, a.radius, 0.0, "Zero-length vector: check radius")
        assertEquals(0.0, a.theta, 0.0, "Zero-length vector: check theta")
        assertEquals(0.0, a.y, 0.0, "Zero-length vector: check y")

        a.setFromVector3(c)
        // radius via sqrt, theta via atan2 → transcendental, use eps; y is a plain copy.
        assertEquals(expected.radius, a.radius, eps, "Normal vector: check radius")
        assertEquals(expected.theta, a.theta, eps, "Normal vector: check theta")
        assertEquals(expected.y, a.y, eps, "Normal vector: check y")
    }
}
