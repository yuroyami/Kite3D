/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Line3.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Line3Test {

    @Test
    fun instancing() {
        var a = Line3()
        assertTrue(a.start == zero3)
        assertTrue(a.end == zero3)

        a = Line3(two3.clone(), one3.clone())
        assertTrue(a.start == two3)
        assertTrue(a.end == one3)
    }

    @Test
    fun set() {
        val a = Line3()

        a.set(one3, one3)
        assertTrue(a.start == one3)
        assertTrue(a.end == one3)
    }

    @Test
    fun copyEquals() {
        val a = Line3(zero3.clone(), one3.clone())
        val b = Line3().copy(a)
        assertTrue(b.start == zero3)
        assertTrue(b.end == one3)

        // ensure that it is a true copy.
        // Upstream reassigns a.start/a.end (fresh objects); here start/end are `val`,
        // so we mutate a's contents in place instead. The intent is the same:
        // b must be unaffected by later changes to a.
        a.start.set(0.0, 0.0, 0.0)
        a.end.set(1.0, 1.0, 1.0)
        assertTrue(b.start == zero3)
        assertTrue(b.end == one3)
    }

    @Test
    fun cloneEqual() {
        var a = Line3()
        val b = Line3(zero3, Vector3(1.0, 1.0, 1.0))
        val c = Line3(zero3, Vector3(1.0, 1.0, 0.0))

        assertFalse(a == b, "Check a and b aren't equal")
        assertFalse(a == c, "Check a and c aren't equal")
        assertFalse(b == c, "Check b and c aren't equal")

        a = b.clone()
        assertTrue(a == b, "Check a and b are equal after clone()")
        assertFalse(a == c, "Check a and c aren't equal after clone()")

        a.set(zero3, zero3)
        assertFalse(a == b, "Check a and b are not equal after modification")
    }

    @Test
    fun getCenter() {
        val center = Vector3()

        val a = Line3(zero3.clone(), two3.clone())
        assertTrue(a.getCenter(center) == one3.clone())
    }

    @Test
    fun delta() {
        val delta = Vector3()

        val a = Line3(zero3.clone(), two3.clone())
        assertTrue(a.delta(delta) == two3.clone())
    }

    @Test
    fun distanceSq() {
        val a = Line3(zero3, zero3)
        val b = Line3(zero3, one3)
        val c = Line3(one3.clone().negate(), one3)
        val d = Line3(two3.clone().multiplyScalar(-2.0), two3.clone().negate())

        // Algebraic (only + - * / sqrt): exact.
        assertEquals(0.0, a.distanceSq(), 0.0, "Check squared distance for zero-length line")
        assertEquals(3.0, b.distanceSq(), 0.0, "Check squared distance for simple line")
        assertEquals(12.0, c.distanceSq(), 0.0, "Check squared distance for negative to positive endpoints")
        assertEquals(12.0, d.distanceSq(), 0.0, "Check squared distance for negative to negative endpoints")
    }

    @Test
    fun distance() {
        val a = Line3(zero3, zero3)
        val b = Line3(zero3, one3)
        val c = Line3(one3.clone().negate(), one3)
        val d = Line3(two3.clone().multiplyScalar(-2.0), two3.clone().negate())

        // distance() goes through sqrt (libm): use a tolerance, matching upstream's numEqual.
        assertEquals(0.0, a.distance(), eps, "Check distance for zero-length line")
        assertEquals(sqrt(3.0), b.distance(), eps, "Check distance for simple line")
        assertEquals(sqrt(12.0), c.distance(), eps, "Check distance for negative to positive endpoints")
        assertEquals(sqrt(12.0), d.distance(), eps, "Check distance for negative to negative endpoints")
    }

    @Test
    fun at() {
        val a = Line3(one3.clone(), Vector3(1.0, 1.0, 2.0))
        val point = Vector3()

        a.at(-1.0, point)
        assertTrue(point.distanceTo(Vector3(1.0, 1.0, 0.0)) < 0.0001)
        a.at(0.0, point)
        assertTrue(point.distanceTo(one3.clone()) < 0.0001)
        a.at(1.0, point)
        assertTrue(point.distanceTo(Vector3(1.0, 1.0, 2.0)) < 0.0001)
        a.at(2.0, point)
        assertTrue(point.distanceTo(Vector3(1.0, 1.0, 3.0)) < 0.0001)
    }

    @Test
    fun closestPointToPointAndParameter() {
        val a = Line3(one3.clone(), Vector3(1.0, 1.0, 2.0))
        val point = Vector3()

        // nearby the ray
        assertTrue(a.closestPointToPointParameter(zero3.clone(), true) == 0.0)
        a.closestPointToPoint(zero3.clone(), true, point)
        assertTrue(point.distanceTo(Vector3(1.0, 1.0, 1.0)) < 0.0001)

        // nearby the ray
        assertTrue(a.closestPointToPointParameter(zero3.clone(), false) == -1.0)
        a.closestPointToPoint(zero3.clone(), false, point)
        assertTrue(point.distanceTo(Vector3(1.0, 1.0, 0.0)) < 0.0001)

        // nearby the ray
        assertTrue(a.closestPointToPointParameter(Vector3(1.0, 1.0, 5.0), true) == 1.0)
        a.closestPointToPoint(Vector3(1.0, 1.0, 5.0), true, point)
        assertTrue(point.distanceTo(Vector3(1.0, 1.0, 2.0)) < 0.0001)

        // exactly on the ray
        assertTrue(a.closestPointToPointParameter(one3.clone(), true) == 0.0)
        a.closestPointToPoint(one3.clone(), true, point)
        assertTrue(point.distanceTo(one3.clone()) < 0.0001)

        // degenerate line (zero-length)
        val b = Line3(one3.clone(), one3.clone())
        assertTrue(b.closestPointToPointParameter(zero3.clone(), true) == 0.0)
        b.closestPointToPoint(zero3.clone(), true, point)
        assertTrue(point.distanceTo(one3.clone()) < 0.0001)
    }

    @Test
    fun applyMatrix4() {
        val a = Line3(zero3.clone(), two3.clone())
        val b = Vector4(two3.x, two3.y, two3.z, 1.0)
        val m = Matrix4().makeTranslation(x, y, z)
        val v = Vector3(x, y, z)

        a.applyMatrix4(m)
        assertTrue(a.start == v, "Translation: check start")
        assertTrue(a.end == Vector3(2 + x, 2 + y, 2 + z), "Translation: check end")

        // reset starting conditions
        a.set(zero3.clone(), two3.clone())
        m.makeRotationX(kotlin.math.PI)

        a.applyMatrix4(m)
        b.applyMatrix4(m)

        assertTrue(a.start == zero3, "Rotation: check start")
        // Transcendental (rotation through sin/cos): tolerance.
        assertEquals(b.x / b.w, a.end.x, eps, "Rotation: check end.x")
        assertEquals(b.y / b.w, a.end.y, eps, "Rotation: check end.y")
        assertEquals(b.z / b.w, a.end.z, eps, "Rotation: check end.z")

        // reset starting conditions
        a.set(zero3.clone(), two3.clone())
        b.set(two3.x, two3.y, two3.z, 1.0)
        m.setPosition(v)

        a.applyMatrix4(m)
        b.applyMatrix4(m)

        assertTrue(a.start == v, "Both: check start")
        assertEquals(b.x / b.w, a.end.x, eps, "Both: check end.x")
        assertEquals(b.y / b.w, a.end.y, eps, "Both: check end.y")
        assertEquals(b.z / b.w, a.end.z, eps, "Both: check end.z")
    }

    @Test
    fun equalsTest() {
        val a = Line3(zero3.clone(), zero3.clone())
        val b = Line3()
        assertTrue(a == b)
    }

    @Test
    fun distanceSqToLine3() {
        val line1 = Line3()
        line1.start.set(0.0, 0.0, 0.0)
        line1.end.set(2.0, 0.0, 0.0)

        val line2 = Line3()
        line2.start.set(1.0, 10.0, 0.0)
        line2.end.set(1.0, -2.0, 0.0)

        // distanceSqToLine3 result comes through non-exact divisions: use a tolerance,
        // matching upstream's numEqual.
        assertEquals(0.0, line1.distanceSqToLine3(line2), eps)

        // Parallel lines case
        line2.start.set(-2.0, 0.0, 2.0)
        line2.end.set(20.0, 0.0, 2.0)

        assertEquals(4.0, line1.distanceSqToLine3(line2), eps)

        // Closest point on lines from one side is out of segment
        line1.start.set(0.0, 4.0, 0.0)
        line1.end.set(2.0, 2.0, 0.0)

        line2.start.set(0.0, 0.0, 0.0)
        line2.end.set(4.0, 0.0, 0.0)

        assertEquals(4.0, line1.distanceSqToLine3(line2), eps)

        // Closest point on lines from another side is out of segment
        line1.start.set(0.0, 4.0, 0.0)
        line1.end.set(3.0, 1.0, 0.0)

        line2.start.set(0.0, 0.0, 0.0)
        line2.end.set(1.0, 0.0, 0.0)

        assertEquals(4.5, line1.distanceSqToLine3(line2), eps)

        // Closest point on lines from both sides is out of the segment
        line1.start.set(0.0, 4.0, 0.0)
        line1.end.set(2.0, 2.0, 0.0)

        line2.start.set(0.0, 0.0, 0.0)
        line2.end.set(1.0, 0.0, 0.0)

        assertEquals(5.0, line1.distanceSqToLine3(line2), eps)

        // General case with skew lines
        line1.start.set(4.0, 0.0, 0.0)
        line1.end.set(-4.0, 0.0, 0.0)

        line2.start.set(0.0, 4.0, 0.0)
        line2.end.set(0.0, 0.0, 4.0)

        assertEquals(8.0, line1.distanceSqToLine3(line2), eps)
    }
}
