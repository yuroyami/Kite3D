/*
 * Copyright © 2026 yuroyami — MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Plane.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Mirrors the `comparePlane` helper from Plane.tests.js. */
private fun comparePlane(a: Plane, b: Plane, threshold: Double = 0.0001): Boolean =
    a.normal.distanceTo(b.normal) < threshold &&
        abs(a.constant - b.constant) < threshold

class PlaneTest {

    @Test
    fun instancing() {
        var a = Plane()
        assertTrue(a.normal.x == 1.0)
        assertTrue(a.normal.y == 0.0)
        assertTrue(a.normal.z == 0.0)
        assertTrue(a.constant == 0.0)

        a = Plane(one3.clone(), 0.0)
        assertTrue(a.normal.x == 1.0)
        assertTrue(a.normal.y == 1.0)
        assertTrue(a.normal.z == 1.0)
        assertTrue(a.constant == 0.0)

        a = Plane(one3.clone(), 1.0)
        assertTrue(a.normal.x == 1.0)
        assertTrue(a.normal.y == 1.0)
        assertTrue(a.normal.z == 1.0)
        assertTrue(a.constant == 1.0)
    }

    // three.js's `isPlane` duck-typing flag is intentionally dropped (dialect rule
    // 10/11): type identity is expressed with Kotlin's `is Plane`, which needs no test.

    @Test
    fun set() {
        val a = Plane()
        assertTrue(a.normal.x == 1.0)
        assertTrue(a.normal.y == 0.0)
        assertTrue(a.normal.z == 0.0)
        assertTrue(a.constant == 0.0)

        val b = a.clone().set(Vector3(x, y, z), w)
        assertTrue(b.normal.x == x)
        assertTrue(b.normal.y == y)
        assertTrue(b.normal.z == z)
        assertTrue(b.constant == w)
    }

    @Test
    fun setComponents() {
        val a = Plane()
        assertTrue(a.normal.x == 1.0)
        assertTrue(a.normal.y == 0.0)
        assertTrue(a.normal.z == 0.0)
        assertTrue(a.constant == 0.0)

        val b = a.clone().setComponents(x, y, z, w)
        assertTrue(b.normal.x == x)
        assertTrue(b.normal.y == y)
        assertTrue(b.normal.z == z)
        assertTrue(b.constant == w)
    }

    @Test
    fun setFromNormalAndCoplanarPoint() {
        val normal = one3.clone().normalize()
        val a = Plane().setFromNormalAndCoplanarPoint(normal, zero3)

        assertTrue(a.normal == normal)
        assertTrue(a.constant == 0.0)
    }

    @Test
    fun setFromCoplanarPoints() {
        val a = Plane()
        val v1 = Vector3(2.0, 0.5, 0.25)
        val v2 = Vector3(2.0, -0.5, 1.25)
        val v3 = Vector3(2.0, -3.5, 2.2)
        val normal = Vector3(1.0, 0.0, 0.0)
        val constant = -2.0

        a.setFromCoplanarPoints(v1, v2, v3)

        assertTrue(a.normal == normal, "Check normal")
        // Algebraic (normal is exactly (1,0,0); constant = -dot): exact.
        assertEquals(constant, a.constant, 0.0, "Check constant")
    }

    @Test
    fun clone() {
        val a = Plane(Vector3(2.0, 0.5, 0.25))
        val b = a.clone()

        assertTrue(a == b, "clones are equal")
    }

    @Test
    fun copy() {
        val a = Plane(Vector3(x, y, z), w)
        val b = Plane().copy(a)
        assertTrue(b.normal.x == x)
        assertTrue(b.normal.y == y)
        assertTrue(b.normal.z == z)
        assertTrue(b.constant == w)

        // ensure that it is a true copy
        a.normal.x = 0.0
        a.normal.y = -1.0
        a.normal.z = -2.0
        a.constant = -3.0
        assertTrue(b.normal.x == x)
        assertTrue(b.normal.y == y)
        assertTrue(b.normal.z == z)
        assertTrue(b.constant == w)
    }

    @Test
    fun normalize() {
        val a = Plane(Vector3(2.0, 0.0, 0.0), 2.0)

        a.normalize()
        assertTrue(a.normal.length() == 1.0)
        assertTrue(a.normal == Vector3(1.0, 0.0, 0.0))
        assertTrue(a.constant == 1.0)
    }

    @Test
    fun negateAndDistanceToPoint() {
        val a = Plane(Vector3(2.0, 0.0, 0.0), -2.0)

        a.normalize()
        assertTrue(a.distanceToPoint(Vector3(4.0, 0.0, 0.0)) == 3.0)
        assertTrue(a.distanceToPoint(Vector3(1.0, 0.0, 0.0)) == 0.0)

        a.negate()
        assertTrue(a.distanceToPoint(Vector3(4.0, 0.0, 0.0)) == -3.0)
        assertTrue(a.distanceToPoint(Vector3(1.0, 0.0, 0.0)) == 0.0)
    }

    @Test
    fun distanceToPoint() {
        val a = Plane(Vector3(2.0, 0.0, 0.0), -2.0)
        val point = Vector3()

        a.normalize().projectPoint(zero3.clone(), point)
        assertTrue(a.distanceToPoint(point) == 0.0)
        assertTrue(a.distanceToPoint(Vector3(4.0, 0.0, 0.0)) == 3.0)
    }

    @Test
    fun distanceToSphere() {
        val a = Plane(Vector3(1.0, 0.0, 0.0), 0.0)

        val b = Sphere(Vector3(2.0, 0.0, 0.0), 1.0)

        assertTrue(a.distanceToSphere(b) == 1.0)

        a.set(Vector3(1.0, 0.0, 0.0), 2.0)
        assertTrue(a.distanceToSphere(b) == 3.0)
        a.set(Vector3(1.0, 0.0, 0.0), -2.0)
        assertTrue(a.distanceToSphere(b) == -1.0)
    }

    @Test
    fun projectPoint() {
        var a = Plane(Vector3(1.0, 0.0, 0.0), 0.0)
        val point = Vector3()

        a.projectPoint(Vector3(10.0, 0.0, 0.0), point)
        assertTrue(point == zero3)
        a.projectPoint(Vector3(-10.0, 0.0, 0.0), point)
        assertTrue(point == zero3)

        a = Plane(Vector3(0.0, 1.0, 0.0), -1.0)
        a.projectPoint(Vector3(0.0, 0.0, 0.0), point)
        assertTrue(point == Vector3(0.0, 1.0, 0.0))
        a.projectPoint(Vector3(0.0, 1.0, 0.0), point)
        assertTrue(point == Vector3(0.0, 1.0, 0.0))
    }

    @Test
    fun intersectLine() {
        var a = Plane(Vector3(1.0, 0.0, 0.0), 0.0)
        val point = Vector3()

        val l1 = Line3(Vector3(-10.0, 0.0, 0.0), Vector3(10.0, 0.0, 0.0))
        a.intersectLine(l1, point)
        assertTrue(point == Vector3(0.0, 0.0, 0.0))

        a = Plane(Vector3(1.0, 0.0, 0.0), -3.0)
        a.intersectLine(l1, point)
        assertTrue(point == Vector3(3.0, 0.0, 0.0))

        // plane lies outside the segment's endpoints
        a = Plane(Vector3(1.0, 0.0, 0.0), -20.0)
        val l2 = Line3(Vector3(-10.0, 0.0, 0.0), Vector3(10.0, 0.0, 0.0))

        assertNull(a.intersectLine(l2, point), "Default clamps to segment and returns null")
        assertNull(a.intersectLine(l2, point, true), "Explicit clampToLine=true returns null")

        val result = a.intersectLine(l2, point, false)
        assertSame(point, result, "clampToLine=false returns the target vector")
        assertTrue(point == Vector3(20.0, 0.0, 0.0), "clampToLine=false returns infinite-line intersection")
    }

    @Test
    fun intersectsBox() {
        val a = Box3(zero3.clone(), one3.clone())
        val b = Plane(Vector3(0.0, 1.0, 0.0), 1.0)
        val c = Plane(Vector3(0.0, 1.0, 0.0), 1.25)
        val d = Plane(Vector3(0.0, -1.0, 0.0), 1.25)
        val e = Plane(Vector3(0.0, 1.0, 0.0), 0.25)
        val f = Plane(Vector3(0.0, 1.0, 0.0), -0.25)
        val g = Plane(Vector3(0.0, 1.0, 0.0), -0.75)
        val h = Plane(Vector3(0.0, 1.0, 0.0), -1.0)
        val i = Plane(Vector3(1.0, 1.0, 1.0).normalize(), -1.732)
        val j = Plane(Vector3(1.0, 1.0, 1.0).normalize(), -1.733)

        assertFalse(b.intersectsBox(a))
        assertFalse(c.intersectsBox(a))
        assertFalse(d.intersectsBox(a))
        assertFalse(e.intersectsBox(a))
        assertTrue(f.intersectsBox(a))
        assertTrue(g.intersectsBox(a))
        assertTrue(h.intersectsBox(a))
        assertTrue(i.intersectsBox(a))
        assertFalse(j.intersectsBox(a))
    }

    @Test
    fun intersectsSphere() {
        val a = Sphere(zero3.clone(), 1.0)
        val b = Plane(Vector3(0.0, 1.0, 0.0), 1.0)
        val c = Plane(Vector3(0.0, 1.0, 0.0), 1.25)
        val d = Plane(Vector3(0.0, -1.0, 0.0), 1.25)

        assertTrue(b.intersectsSphere(a))
        assertFalse(c.intersectsSphere(a))
        assertFalse(d.intersectsSphere(a))
    }

    @Test
    fun coplanarPoint() {
        val point = Vector3()

        var a = Plane(Vector3(1.0, 0.0, 0.0), 0.0)
        a.coplanarPoint(point)
        assertTrue(a.distanceToPoint(point) == 0.0)

        a = Plane(Vector3(0.0, 1.0, 0.0), -1.0)
        a.coplanarPoint(point)
        assertTrue(a.distanceToPoint(point) == 0.0)
    }

    @Test
    fun applyMatrix4AndTranslate() {
        var a = Plane(Vector3(1.0, 0.0, 0.0), 0.0)

        val m = Matrix4()
        m.makeRotationZ(PI * 0.5)

        assertTrue(comparePlane(a.clone().applyMatrix4(m), Plane(Vector3(0.0, 1.0, 0.0), 0.0)))

        a = Plane(Vector3(0.0, 1.0, 0.0), -1.0)
        assertTrue(comparePlane(a.clone().applyMatrix4(m), Plane(Vector3(-1.0, 0.0, 0.0), -1.0)))

        m.makeTranslation(1.0, 1.0, 1.0)
        assertTrue(comparePlane(a.clone().applyMatrix4(m), a.clone().translate(Vector3(1.0, 1.0, 1.0))))
    }

    @Test
    fun equalsTest() {
        val a = Plane(Vector3(1.0, 0.0, 0.0), 0.0)
        val b = Plane(Vector3(1.0, 0.0, 0.0), 1.0)
        val c = Plane(Vector3(0.0, 1.0, 0.0), 0.0)

        assertTrue(a.normal == b.normal, "Normals: equal")
        assertFalse(a.normal == c.normal, "Normals: not equal")

        assertTrue(a.constant != b.constant, "Constants: not equal")
        assertTrue(a.constant == c.constant, "Constants: equal")

        assertFalse(a == b, "Planes: not equal")
        assertFalse(a == c, "Planes: not equal")

        a.copy(b)
        assertTrue(a.normal == b.normal, "Normals after copy(): equal")
        assertEquals(b.constant, a.constant, 0.0, "Constants after copy(): equal")
        assertTrue(a == b, "Planes after copy(): equal")
    }
}
