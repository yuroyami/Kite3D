/*
 * Copyright © 2026 yuroyami — MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Quaternion.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuaternionTest {

    private val orders = listOf(
        EulerOrder.XYZ, EulerOrder.YXZ, EulerOrder.ZXY,
        EulerOrder.ZYX, EulerOrder.YZX, EulerOrder.XZY,
    )
    private val eulerAngles get() = Euler(0.1, -0.3, 0.25)

    // Number.EPSILON
    private val numberEpsilon = 2.220446049250313e-16

    // Math.SQRT1_2
    private val sqrt1_2 = 1.0 / sqrt(2.0)

    private fun qSub(a: Quaternion, b: Quaternion): Quaternion {
        val result = Quaternion()
        result.copy(a)

        result.x -= b.x
        result.y -= b.y
        result.z -= b.z
        result.w -= b.w

        return result
    }

    private fun changeEulerOrder(euler: Euler, order: EulerOrder): Euler =
        Euler(euler.x, euler.y, euler.z, order)

    /** The result of a slerp, exposing the same shape as the JS test closures. */
    private class SlerpResult(
        val x: Double,
        val y: Double,
        val z: Double,
        val w: Double,
        val length: Double,
        val dotA: Double,
        val dotB: Double,
    ) {
        fun equals(x: Double, y: Double, z: Double, w: Double, maxError: Double): Boolean =
            abs(x - this.x) <= maxError &&
                abs(y - this.y) <= maxError &&
                abs(z - this.z) <= maxError &&
                abs(w - this.w) <= maxError
    }

    private fun doSlerpObject(aArr: DoubleArray, bArr: DoubleArray, t: Double): SlerpResult {
        val a = Quaternion().fromArray(aArr)
        val b = Quaternion().fromArray(bArr)
        val c = Quaternion().fromArray(aArr)

        c.slerp(b, t)

        return SlerpResult(c.x, c.y, c.z, c.w, c.length(), c.dot(a), c.dot(b))
    }

    private fun doSlerpArray(a: DoubleArray, b: DoubleArray, t: Double): SlerpResult {
        val result = doubleArrayOf(0.0, 0.0, 0.0, 0.0)

        Quaternion.slerpFlat(result, 0, a, 0, b, 0, t)

        fun arrDot(x: DoubleArray, y: DoubleArray): Double =
            x[0] * y[0] + x[1] * y[1] + x[2] * y[2] + x[3] * y[3]

        return SlerpResult(
            result[0], result[1], result[2], result[3],
            sqrt(arrDot(result, result)),
            arrDot(result, a),
            arrDot(result, b),
        )
    }

    private fun slerpTestSkeleton(doSlerp: (DoubleArray, DoubleArray, Double) -> SlerpResult, maxError: Double) {
        var result: SlerpResult

        val a = doubleArrayOf(
            0.6753410084407496,
            0.4087830051091744,
            0.32856700410659473,
            0.5185120064806223,
        )

        val b = doubleArrayOf(
            0.6602792107657797,
            0.43647413932562285,
            0.35119011210236006,
            0.5001871596632682,
        )

        var maxNormError = 0.0

        fun isNormal(res: SlerpResult): Boolean {
            val normError = abs(1 - res.length)
            maxNormError = maxOf(maxNormError, normError)
            return normError <= maxError
        }

        result = doSlerp(a, b, 0.0)
        assertTrue(result.equals(a[0], a[1], a[2], a[3], 0.0), "Exactly A @ t = 0")

        result = doSlerp(a, b, 1.0)
        assertTrue(result.equals(b[0], b[1], b[2], b[3], 0.0), "Exactly B @ t = 1")

        result = doSlerp(a, b, 0.5)
        assertTrue(abs(result.dotA - result.dotB) <= numberEpsilon, "Symmetry at 0.5")
        assertTrue(isNormal(result), "Approximately normal (at 0.5)")

        result = doSlerp(a, b, 0.25)
        assertTrue(result.dotA > result.dotB, "Interpolating at 0.25")
        assertTrue(isNormal(result), "Approximately normal (at 0.25)")

        result = doSlerp(a, b, 0.75)
        assertTrue(result.dotA < result.dotB, "Interpolating at 0.75")
        assertTrue(isNormal(result), "Approximately normal (at 0.75)")

        val d = sqrt1_2

        result = doSlerp(doubleArrayOf(1.0, 0.0, 0.0, 0.0), doubleArrayOf(0.0, 0.0, 1.0, 0.0), 0.5)
        assertTrue(result.equals(d, 0.0, d, 0.0, numberEpsilon), "X/Z diagonal from axes")
        assertTrue(isNormal(result), "Approximately normal (X/Z diagonal)")

        result = doSlerp(doubleArrayOf(0.0, d, 0.0, d), doubleArrayOf(0.0, -d, 0.0, d), 0.5)
        assertTrue(result.equals(0.0, 0.0, 0.0, 1.0, numberEpsilon), "W-Unit from diagonals")
        assertTrue(isNormal(result), "Approximately normal (W-Unit)")
    }

    @Test
    fun instancing() {
        var a = Quaternion()
        assertTrue(a.x == 0.0, "Passed!")
        assertTrue(a.y == 0.0, "Passed!")
        assertTrue(a.z == 0.0, "Passed!")
        assertTrue(a.w == 1.0, "Passed!")

        a = Quaternion(x, y, z, w)
        assertTrue(a.x == x, "Passed!")
        assertTrue(a.y == y, "Passed!")
        assertTrue(a.z == z, "Passed!")
        assertTrue(a.w == w, "Passed!")
    }

    @Test
    fun slerpStatic() {
        slerpTestSkeleton(::doSlerpObject, numberEpsilon)
    }

    @Test
    fun slerpFlat() {
        slerpTestSkeleton(::doSlerpArray, numberEpsilon)
    }

    @Test
    fun properties() {
        var callbacks = 0
        val a = Quaternion()
        a.onChange { callbacks++ }

        a.x = x
        a.y = y
        a.z = z
        a.w = w

        assertEquals(x, a.x, 0.0, "Check x")
        assertEquals(y, a.y, 0.0, "Check y")
        assertEquals(z, a.z, 0.0, "Check z")
        assertEquals(w, a.w, 0.0, "Check w")

        assertEquals(4, callbacks, "onChange fired once per setter")
    }

    @Test
    fun x() {
        var a = Quaternion()
        assertTrue(a.x == 0.0, "Passed!")

        a = Quaternion(1.0, 2.0, 3.0)
        assertTrue(a.x == 1.0, "Passed!")

        a = Quaternion(4.0, 5.0, 6.0, 1.0)
        assertTrue(a.x == 4.0, "Passed!")

        a = Quaternion(7.0, 8.0, 9.0)
        a.x = 10.0
        assertTrue(a.x == 10.0, "Passed!")

        a = Quaternion(11.0, 12.0, 13.0)
        var b = false
        a.onChange { b = true }
        assertFalse(b, "Passed!")
        a.x = 14.0
        assertTrue(b, "Passed!")
        assertTrue(a.x == 14.0, "Passed!")
    }

    @Test
    fun y() {
        var a = Quaternion()
        assertTrue(a.y == 0.0, "Passed!")

        a = Quaternion(1.0, 2.0, 3.0)
        assertTrue(a.y == 2.0, "Passed!")

        a = Quaternion(4.0, 5.0, 6.0, 1.0)
        assertTrue(a.y == 5.0, "Passed!")

        a = Quaternion(7.0, 8.0, 9.0)
        a.y = 10.0
        assertTrue(a.y == 10.0, "Passed!")

        a = Quaternion(11.0, 12.0, 13.0)
        var b = false
        a.onChange { b = true }
        assertFalse(b, "Passed!")
        a.y = 14.0
        assertTrue(b, "Passed!")
        assertTrue(a.y == 14.0, "Passed!")
    }

    @Test
    fun z() {
        var a = Quaternion()
        assertTrue(a.z == 0.0, "Passed!")

        a = Quaternion(1.0, 2.0, 3.0)
        assertTrue(a.z == 3.0, "Passed!")

        a = Quaternion(4.0, 5.0, 6.0, 1.0)
        assertTrue(a.z == 6.0, "Passed!")

        a = Quaternion(7.0, 8.0, 9.0)
        a.z = 10.0
        assertTrue(a.z == 10.0, "Passed!")

        a = Quaternion(11.0, 12.0, 13.0)
        var b = false
        a.onChange { b = true }
        assertFalse(b, "Passed!")
        a.z = 14.0
        assertTrue(b, "Passed!")
        assertTrue(a.z == 14.0, "Passed!")
    }

    @Test
    fun w() {
        var a = Quaternion()
        assertTrue(a.w == 1.0, "Passed!")

        a = Quaternion(1.0, 2.0, 3.0)
        assertTrue(a.w == 1.0, "Passed!")

        a = Quaternion(4.0, 5.0, 6.0, 1.0)
        assertTrue(a.w == 1.0, "Passed!")

        a = Quaternion(7.0, 8.0, 9.0)
        a.w = 10.0
        assertTrue(a.w == 10.0, "Passed!")

        a = Quaternion(11.0, 12.0, 13.0)
        var b = false
        a.onChange { b = true }
        assertFalse(b, "Passed!")
        a.w = 14.0
        assertTrue(b, "Passed!")
        assertTrue(a.w == 14.0, "Passed!")
    }

    // The upstream `isQuaternion` test checks a duck-type flag; dialect rule 10
    // drops these flags (`is Quaternion` is used instead), so it is omitted.

    @Test
    fun set() {
        val a = Quaternion()
        assertTrue(a.x == 0.0, "Passed!")
        assertTrue(a.y == 0.0, "Passed!")
        assertTrue(a.z == 0.0, "Passed!")
        assertTrue(a.w == 1.0, "Passed!")

        a.set(x, y, z, w)
        assertTrue(a.x == x, "Passed!")
        assertTrue(a.y == y, "Passed!")
        assertTrue(a.z == z, "Passed!")
        assertTrue(a.w == w, "Passed!")
    }

    @Test
    fun clone() {
        val a = Quaternion().clone()
        assertTrue(a.x == 0.0, "Passed!")
        assertTrue(a.y == 0.0, "Passed!")
        assertTrue(a.z == 0.0, "Passed!")
        assertTrue(a.w == 1.0, "Passed!")

        val b = a.set(x, y, z, w).clone()
        assertTrue(b.x == x, "Passed!")
        assertTrue(b.y == y, "Passed!")
        assertTrue(b.z == z, "Passed!")
        assertTrue(b.w == w, "Passed!")
    }

    @Test
    fun copy() {
        val a = Quaternion(x, y, z, w)
        val b = Quaternion().copy(a)
        assertTrue(b.x == x, "Passed!")
        assertTrue(b.y == y, "Passed!")
        assertTrue(b.z == z, "Passed!")
        assertTrue(b.w == w, "Passed!")

        // ensure that it is a true copy
        a.x = 0.0
        a.y = -1.0
        a.z = 0.0
        a.w = -1.0
        assertTrue(b.x == x, "Passed!")
        assertTrue(b.y == y, "Passed!")
    }

    @Test
    fun setFromEulerSetFromQuaternion() {
        val angles = listOf(Vector3(1.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0), Vector3(0.0, 0.0, 1.0))

        // ensure euler conversion to/from Quaternion matches.
        for (order in orders) {
            for (angle in angles) {
                val eulers2 = Euler().setFromQuaternion(
                    Quaternion().setFromEuler(Euler(angle.x, angle.y, angle.z, order)),
                    order,
                )
                val newAngle = Vector3(eulers2.x, eulers2.y, eulers2.z)
                assertTrue(newAngle.distanceTo(angle) < 0.001, "Passed!")
            }
        }
    }

    @Test
    fun setFromAxisAngle() {
        val zero = Quaternion()

        var a = Quaternion().setFromAxisAngle(Vector3(1.0, 0.0, 0.0), 0.0)
        assertTrue(a == zero, "Passed!")
        a = Quaternion().setFromAxisAngle(Vector3(0.0, 1.0, 0.0), 0.0)
        assertTrue(a == zero, "Passed!")
        a = Quaternion().setFromAxisAngle(Vector3(0.0, 0.0, 1.0), 0.0)
        assertTrue(a == zero, "Passed!")

        val b1 = Quaternion().setFromAxisAngle(Vector3(1.0, 0.0, 0.0), PI)
        assertFalse(a == b1, "Passed!")
        val b2 = Quaternion().setFromAxisAngle(Vector3(1.0, 0.0, 0.0), -PI)
        assertFalse(a == b2, "Passed!")

        b1.multiply(b2)
        assertTrue(a == b1, "Passed!")
    }

    @Test
    fun setFromEulerSetFromRotationMatrix() {
        // ensure euler conversion for Quaternion matches that of Matrix4
        for (order in orders) {
            val q = Quaternion().setFromEuler(changeEulerOrder(eulerAngles, order))
            val m = Matrix4().makeRotationFromEuler(changeEulerOrder(eulerAngles, order))
            val q2 = Quaternion().setFromRotationMatrix(m)

            assertTrue(qSub(q, q2).length() < 0.001, "Passed!")
        }
    }

    @Test
    fun setFromRotationMatrix() {
        // contrived examples purely to please the god of code coverage...
        // match conditions in various 'else [if]' blocks

        val a = Quaternion()
        var q = Quaternion(-9.0, -2.0, 3.0, -4.0).normalize()
        val m = Matrix4().makeRotationFromQuaternion(q)
        var expected = Vector4(0.8581163303210332, 0.19069251784911848, -0.2860387767736777, 0.38138503569823695)

        a.setFromRotationMatrix(m)
        assertTrue(abs(a.x - expected.x) <= eps, "m11 > m22 && m11 > m33: check x")
        assertTrue(abs(a.y - expected.y) <= eps, "m11 > m22 && m11 > m33: check y")
        assertTrue(abs(a.z - expected.z) <= eps, "m11 > m22 && m11 > m33: check z")
        assertTrue(abs(a.w - expected.w) <= eps, "m11 > m22 && m11 > m33: check w")

        q = Quaternion(-1.0, -2.0, 1.0, -1.0).normalize()
        m.makeRotationFromQuaternion(q)
        expected = Vector4(0.37796447300922714, 0.7559289460184544, -0.37796447300922714, 0.37796447300922714)

        a.setFromRotationMatrix(m)
        assertTrue(abs(a.x - expected.x) <= eps, "m22 > m33: check x")
        assertTrue(abs(a.y - expected.y) <= eps, "m22 > m33: check y")
        assertTrue(abs(a.z - expected.z) <= eps, "m22 > m33: check z")
        assertTrue(abs(a.w - expected.w) <= eps, "m22 > m33: check w")
    }

    @Test
    fun setFromUnitVectors() {
        val a = Quaternion()
        val b = Vector3(1.0, 0.0, 0.0)
        val c = Vector3(0.0, 1.0, 0.0)
        val expected = Quaternion(0.0, 0.0, sqrt(2.0) / 2, sqrt(2.0) / 2)

        a.setFromUnitVectors(b, c)
        assertTrue(abs(a.x - expected.x) <= eps, "Check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Check z")
        assertTrue(abs(a.w - expected.w) <= eps, "Check w")
    }

    @Test
    fun angleTo() {
        val a = Quaternion()
        val b = Quaternion().setFromEuler(Euler(0.0, PI, 0.0))
        val c = Quaternion().setFromEuler(Euler(0.0, PI * 2, 0.0))

        assertTrue(a.angleTo(a) == 0.0, "Passed!")
        assertEquals(PI, a.angleTo(b), 1e-12, "Passed!")
        assertEquals(0.0, a.angleTo(c), 1e-12, "Passed!")
    }

    @Test
    fun rotateTowards() {
        val a = Quaternion()
        val b = Quaternion().setFromEuler(Euler(0.0, PI, 0.0))
        val c = Quaternion()

        val halfPI = PI * 0.5

        a.rotateTowards(b, 0.0)
        assertTrue(a == a, "Passed!")

        a.rotateTowards(b, PI * 2) // test overshoot
        assertTrue(a == b, "Passed!")

        a.set(0.0, 0.0, 0.0, 1.0)
        a.rotateTowards(b, halfPI)
        assertTrue(a.angleTo(c) - halfPI <= eps, "Passed!")
    }

    @Test
    fun identity() {
        val a = Quaternion()

        a.set(x, y, z, w)
        a.identity()

        assertTrue(a.x == 0.0, "Passed!")
        assertTrue(a.y == 0.0, "Passed!")
        assertTrue(a.z == 0.0, "Passed!")
        assertTrue(a.w == 1.0, "Passed!")
    }

    @Test
    fun invertConjugate() {
        val a = Quaternion(x, y, z, w)

        val b = a.clone().conjugate()

        assertTrue(a.x == -b.x, "Passed!")
        assertTrue(a.y == -b.y, "Passed!")
        assertTrue(a.z == -b.z, "Passed!")
        assertTrue(a.w == b.w, "Passed!")
    }

    @Test
    fun dot() {
        var a = Quaternion()
        var b = Quaternion()

        assertTrue(a.dot(b) == 1.0, "Passed!")
        a = Quaternion(1.0, 2.0, 3.0, 1.0)
        b = Quaternion(3.0, 2.0, 1.0, 1.0)

        assertTrue(a.dot(b) == 11.0, "Passed!")
    }

    @Test
    fun normalizeLengthLengthSq() {
        val a = Quaternion(x, y, z, w)

        assertTrue(a.length() != 1.0, "Passed!")
        assertTrue(a.lengthSq() != 1.0, "Passed!")
        a.normalize()
        assertTrue(a.length() == 1.0, "Passed!")
        assertTrue(a.lengthSq() == 1.0, "Passed!")

        a.set(0.0, 0.0, 0.0, 0.0)
        assertTrue(a.lengthSq() == 0.0, "Passed!")
        assertTrue(a.length() == 0.0, "Passed!")
        a.normalize()
        assertTrue(a.lengthSq() == 1.0, "Passed!")
        assertTrue(a.length() == 1.0, "Passed!")
    }

    @Test
    fun multiplyQuaternionsMultiply() {
        val angles = listOf(Euler(1.0, 0.0, 0.0), Euler(0.0, 1.0, 0.0), Euler(0.0, 0.0, 1.0))

        val q1 = Quaternion().setFromEuler(changeEulerOrder(angles[0], EulerOrder.XYZ))
        val q2 = Quaternion().setFromEuler(changeEulerOrder(angles[1], EulerOrder.XYZ))
        val q3 = Quaternion().setFromEuler(changeEulerOrder(angles[2], EulerOrder.XYZ))

        val q = Quaternion().multiplyQuaternions(q1, q2).multiply(q3)

        val m1 = Matrix4().makeRotationFromEuler(changeEulerOrder(angles[0], EulerOrder.XYZ))
        val m2 = Matrix4().makeRotationFromEuler(changeEulerOrder(angles[1], EulerOrder.XYZ))
        val m3 = Matrix4().makeRotationFromEuler(changeEulerOrder(angles[2], EulerOrder.XYZ))

        val m = Matrix4().multiplyMatrices(m1, m2).multiply(m3)

        val qFromM = Quaternion().setFromRotationMatrix(m)

        assertTrue(qSub(q, qFromM).length() < 0.001, "Passed!")
    }

    @Test
    fun premultiply() {
        val a = Quaternion(x, y, z, w)
        val b = Quaternion(2 * x, -y, -2 * z, w)
        val expected = Quaternion(42.0, -32.0, -2.0, 58.0)

        a.premultiply(b)
        assertTrue(abs(a.x - expected.x) <= eps, "Check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Check z")
        assertTrue(abs(a.w - expected.w) <= eps, "Check w")
    }

    @Test
    fun slerp() {
        val a = Quaternion(x, y, z, w).normalize()
        val b = Quaternion(w, x, y, z).normalize()

        val c = a.clone().slerp(b, 0.0)
        val d = a.clone().slerp(b, 1.0)

        assertTrue(a == c, "Passed")
        assertTrue(b == d, "Passed")

        val bigD = sqrt1_2

        val e = Quaternion(1.0, 0.0, 0.0, 0.0)
        val f = Quaternion(0.0, 0.0, 1.0, 0.0)
        var expected = Quaternion(bigD, 0.0, bigD, 0.0)
        var result = e.clone().slerp(f, 0.5)
        assertTrue(abs(result.x - expected.x) <= eps, "Check x")
        assertTrue(abs(result.y - expected.y) <= eps, "Check y")
        assertTrue(abs(result.z - expected.z) <= eps, "Check z")
        assertTrue(abs(result.w - expected.w) <= eps, "Check w")

        val g = Quaternion(0.0, bigD, 0.0, bigD)
        val h = Quaternion(0.0, -bigD, 0.0, bigD)
        expected = Quaternion(0.0, 0.0, 0.0, 1.0)
        result = g.clone().slerp(h, 0.5)

        assertTrue(abs(result.x - expected.x) <= eps, "Check x")
        assertTrue(abs(result.y - expected.y) <= eps, "Check y")
        assertTrue(abs(result.z - expected.z) <= eps, "Check z")
        assertTrue(abs(result.w - expected.w) <= eps, "Check w")
    }

    @Test
    fun slerpQuaternions() {
        val e = Quaternion(1.0, 0.0, 0.0, 0.0)
        val f = Quaternion(0.0, 0.0, 1.0, 0.0)
        val expected = Quaternion(sqrt1_2, 0.0, sqrt1_2, 0.0)

        val a = Quaternion()
        a.slerpQuaternions(e, f, 0.5)

        assertTrue(abs(a.x - expected.x) <= eps, "Check x")
        assertTrue(abs(a.y - expected.y) <= eps, "Check y")
        assertTrue(abs(a.z - expected.z) <= eps, "Check z")
        assertTrue(abs(a.w - expected.w) <= eps, "Check w")
    }

    @Test
    fun random() {
        val a = Quaternion()

        a.random()

        val identity = Quaternion()
        // randomizes at least one component of the quaternion (a random rotation
        // effectively never equals the identity).
        assertFalse(a == identity, "randomizes at least one component of the quaternion")

        assertTrue((1 - a.length()) <= numberEpsilon, "produces a normalized quaternion")
    }

    @Test
    fun equalsTest() {
        val a = Quaternion(x, y, z, w)
        val b = Quaternion(-x, -y, -z, -w)

        assertTrue(a.x != b.x, "Passed!")
        assertTrue(a.y != b.y, "Passed!")

        assertFalse(a == b, "Passed!")
        assertFalse(b == a, "Passed!")

        a.copy(b)
        assertTrue(a.x == b.x, "Passed!")
        assertTrue(a.y == b.y, "Passed!")

        assertTrue(a == b, "Passed!")
        assertTrue(b == a, "Passed!")
    }

    @Test
    fun fromArray() {
        val a = Quaternion()
        a.fromArray(doubleArrayOf(x, y, z, w))
        assertTrue(a.x == x, "Passed!")
        assertTrue(a.y == y, "Passed!")
        assertTrue(a.z == z, "Passed!")
        assertTrue(a.w == w, "Passed!")

        // JS passes a leading `undefined` then reads from offset 1; a DoubleArray has
        // no `undefined`, so a 0.0 placeholder stands in for the skipped slot.
        a.fromArray(doubleArrayOf(0.0, x, y, z, w, 0.0), 1)
        assertTrue(a.x == x, "Passed!")
        assertTrue(a.y == y, "Passed!")
        assertTrue(a.z == z, "Passed!")
        assertTrue(a.w == w, "Passed!")
    }

    @Test
    fun toArrayTest() {
        val a = Quaternion(x, y, z, w)

        var array = a.toArray()
        assertEquals(x, array[0], 0.0, "No array, no offset: check x")
        assertEquals(y, array[1], 0.0, "No array, no offset: check y")
        assertEquals(z, array[2], 0.0, "No array, no offset: check z")
        assertEquals(w, array[3], 0.0, "No array, no offset: check w")

        array = mutableListOf()
        a.toArray(array)
        assertEquals(x, array[0], 0.0, "With array, no offset: check x")
        assertEquals(y, array[1], 0.0, "With array, no offset: check y")
        assertEquals(z, array[2], 0.0, "With array, no offset: check z")
        assertEquals(w, array[3], 0.0, "With array, no offset: check w")

        array = mutableListOf()
        a.toArray(array, 1)
        // Kotlin lists have no holes: the gap at [0] is filled with 0.0 (three.js
        // leaves it undefined).
        assertEquals(0.0, array[0], 0.0, "With array and offset: check [0]")
        assertEquals(x, array[1], 0.0, "With array and offset: check x")
        assertEquals(y, array[2], 0.0, "With array and offset: check y")
        assertEquals(z, array[3], 0.0, "With array and offset: check z")
        assertEquals(w, array[4], 0.0, "With array and offset: check w")
    }

    @Test
    fun fromBufferAttribute() {
        val a = Quaternion()

        val attribute = TestBufferAttribute(
            doubleArrayOf(
                0.0, 0.0, 0.0, 1.0,
                .7, 0.0, 0.0, .7,
                0.0, .7, 0.0, .7,
            ),
            4,
        )

        a.fromBufferAttribute(attribute, 0)
        assertEquals(0.0, a.x, eps, "index 0, component x")
        assertEquals(0.0, a.y, eps, "index 0, component y")
        assertEquals(0.0, a.z, eps, "index 0, component z")
        assertEquals(1.0, a.w, eps, "index 0, component w")

        a.fromBufferAttribute(attribute, 1)
        assertEquals(.7, a.x, eps, "index 1, component x")
        assertEquals(0.0, a.y, eps, "index 1, component y")
        assertEquals(0.0, a.z, eps, "index 1, component z")
        assertEquals(.7, a.w, eps, "index 1, component w")

        a.fromBufferAttribute(attribute, 2)
        assertEquals(0.0, a.x, eps, "index 2, component x")
        assertEquals(.7, a.y, eps, "index 2, component y")
        assertEquals(0.0, a.z, eps, "index 2, component z")
        assertEquals(.7, a.w, eps, "index 2, component w")
    }

    @Test
    fun onChangeTest() {
        // Upstream `_onChange` / `_onChangeCallback` store, reassign and invoke the
        // private callback field and assert `this === a` binding. The field is
        // private here and Kotlin lambdas have no `this` receiver to check, so we
        // verify the registered callback actually fires via the public onChange API.
        var fired = false
        val a = Quaternion(11.0, 12.0, 13.0, 1.0)
        a.onChange { fired = true }
        a.x = 1.0
        assertTrue(fired, "registered callback fires on change")
    }

    @Test
    fun multiplyVector3() {
        val angles = listOf(Euler(1.0, 0.0, 0.0), Euler(0.0, 1.0, 0.0), Euler(0.0, 0.0, 1.0))

        // ensure euler conversion for Quaternion matches that of Matrix4
        for (order in orders) {
            for (angle in angles) {
                val q = Quaternion().setFromEuler(changeEulerOrder(angle, order))
                val m = Matrix4().makeRotationFromEuler(changeEulerOrder(angle, order))

                val v0 = Vector3(1.0, 0.0, 0.0)
                val qv = v0.clone().applyQuaternion(q)
                val mv = v0.clone().applyMatrix4(m)

                assertTrue(qv.distanceTo(mv) < 0.001, "Passed!")
            }
        }
    }

    @Test
    fun toJSON() {
        val q = Quaternion(0.0, 0.5, 0.7, 1.0)
        val array = q.toJSON()
        assertEquals(0.0, array[0], 0.0, "Quaternion is serializable.")
        assertEquals(0.5, array[1], 0.0, "Quaternion is serializable.")
        assertEquals(0.7, array[2], 0.0, "Quaternion is serializable.")
        assertEquals(1.0, array[3], 0.0, "Quaternion is serializable.")
    }

    @Test
    fun iterable() {
        val q = Quaternion(0.0, 0.5, 0.7, 1.0)
        val array = q.toList()
        assertEquals(0.0, array[0], 0.0, "Quaternion is iterable.")
        assertEquals(0.5, array[1], 0.0, "Quaternion is iterable.")
        assertEquals(0.7, array[2], 0.0, "Quaternion is iterable.")
        assertEquals(1.0, array[3], 0.0, "Quaternion is iterable.")
    }
}
