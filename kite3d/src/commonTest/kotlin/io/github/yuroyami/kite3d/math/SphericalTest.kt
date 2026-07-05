/*
 * Copyright © 2026 yuroyami — MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Spherical.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SphericalTest {

    @Test
    fun instancing() {
        var a = Spherical()
        val radius = 10.0
        val phi = acos(-0.5)
        val theta = sqrt(PI) * phi

        assertEquals(1.0, a.radius, 0.0, "Default values: check radius")
        assertEquals(0.0, a.phi, 0.0, "Default values: check phi")
        assertEquals(0.0, a.theta, 0.0, "Default values: check theta")

        a = Spherical(radius, phi, theta)
        assertEquals(radius, a.radius, 0.0, "Custom values: check radius")
        assertEquals(phi, a.phi, 0.0, "Custom values: check phi")
        assertEquals(theta, a.theta, 0.0, "Custom values: check theta")
    }

    @Test
    fun set() {
        val a = Spherical()
        val radius = 10.0
        val phi = acos(-0.5)
        val theta = sqrt(PI) * phi

        a.set(radius, phi, theta)
        assertEquals(radius, a.radius, 0.0, "Check radius")
        assertEquals(phi, a.phi, 0.0, "Check phi")
        assertEquals(theta, a.theta, 0.0, "Check theta")
    }

    @Test
    fun clone() {
        val radius = 10.0
        val phi = acos(-0.5)
        val theta = sqrt(PI) * phi
        val a = Spherical(radius, phi, theta)
        val b = a.clone()

        // three.js uses propEqual (structural, all fields) → Kotlin structural `==`.
        assertTrue(a == b, "Check a and b are equal after clone()")

        a.radius = 2.0
        assertFalse(a == b, "Check a and b are not equal after modification")
    }

    @Test
    fun copy() {
        val radius = 10.0
        val phi = acos(-0.5)
        val theta = sqrt(PI) * phi
        val a = Spherical(radius, phi, theta)
        val b = Spherical().copy(a)

        assertTrue(a == b, "Check a and b are equal after copy()")

        a.radius = 2.0
        assertFalse(a == b, "Check a and b are not equal after modification")
    }

    @Test
    fun makeSafe() {
        val EPS = 0.000001 // from source
        val tooLow = 0.0
        val tooHigh = PI
        val justRight = 1.5
        val a = Spherical(1.0, tooLow, 0.0)

        a.makeSafe()
        assertEquals(EPS, a.phi, 0.0, "Check if small values are set to EPS")

        a.set(1.0, tooHigh, 0.0)
        a.makeSafe()
        // PI - EPS is algebraic (subtraction only) → exact.
        assertEquals(PI - EPS, a.phi, 0.0, "Check if high values are set to (Math.PI - EPS)")

        a.set(1.0, justRight, 0.0)
        a.makeSafe()
        assertEquals(justRight, a.phi, 0.0, "Check that valid values don't get changed")
    }

    @Test
    fun setFromVector3() {
        val a = Spherical(1.0, 1.0, 1.0)
        val b = Vector3(0.0, 0.0, 0.0)
        val c = Vector3(PI, 1.0, -PI)
        val expected = Spherical(4.554032147688322, 1.3494066171539107, 2.356194490192345)

        a.setFromVector3(b)
        assertEquals(0.0, a.radius, 0.0, "Zero-length vector: check radius")
        assertEquals(0.0, a.phi, 0.0, "Zero-length vector: check phi")
        assertEquals(0.0, a.theta, 0.0, "Zero-length vector: check theta")

        a.setFromVector3(c)
        // radius via sqrt, phi via acos, theta via atan2 → transcendental, use eps.
        assertEquals(expected.radius, a.radius, eps, "Normal vector: check radius")
        assertEquals(expected.phi, a.phi, eps, "Normal vector: check phi")
        assertEquals(expected.theta, a.theta, eps, "Normal vector: check theta")
    }

    @Test
    fun setFromCartesianCoords() {
        val a = Spherical(1.0, 1.0, 1.0)
        val expected = Spherical(4.554032147688322, 1.3494066171539107, 2.356194490192345)

        a.setFromCartesianCoords(0.0, 0.0, 0.0)
        assertEquals(0.0, a.radius, 0.0, "Zero-length vector: check radius")
        assertEquals(0.0, a.phi, 0.0, "Zero-length vector: check phi")
        assertEquals(0.0, a.theta, 0.0, "Zero-length vector: check theta")

        a.setFromCartesianCoords(PI, 1.0, -PI)
        assertEquals(expected.radius, a.radius, eps, "Normal vector: check radius")
        assertEquals(expected.phi, a.phi, eps, "Normal vector: check phi")
        assertEquals(expected.theta, a.theta, eps, "Normal vector: check theta")
    }
}
