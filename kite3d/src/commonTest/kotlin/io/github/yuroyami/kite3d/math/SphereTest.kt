/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Sphere.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SphereTest {

    @Test
    fun instancing() {
        var a = Sphere()
        assertTrue(a.center == zero3)
        assertTrue(a.radius == -1.0)

        a = Sphere(one3.clone(), 1.0)
        assertTrue(a.center == one3)
        assertTrue(a.radius == 1.0)
    }

    // three.js's `isSphere` duck-typing flag is intentionally dropped (dialect rule
    // 10/11): type identity is expressed with Kotlin's `is Sphere`, which needs no test.

    @Test
    fun set() {
        val a = Sphere()
        assertTrue(a.center == zero3)
        assertTrue(a.radius == -1.0)

        a.set(one3, 1.0)
        assertTrue(a.center == one3)
        assertTrue(a.radius == 1.0)
    }

    @Test
    fun setFromPoints() {
        val a = Sphere()
        val expectedCenter = Vector3(0.9330126941204071, 0.0, 0.0)
        var expectedRadius = 1.3676668773461689
        val optionalCenter = Vector3(1.0, 1.0, 1.0)
        val points = listOf(
            Vector3(1.0, 1.0, 0.0), Vector3(1.0, 1.0, 0.0),
            Vector3(1.0, 1.0, 0.0), Vector3(1.0, 1.0, 0.0),
            Vector3(1.0, 1.0, 0.0), Vector3(0.8660253882408142, 0.5, 0.0),
            Vector3(-0.0, 0.5, 0.8660253882408142), Vector3(1.8660253882408142, 0.5, 0.0),
            Vector3(0.0, 0.5, -0.8660253882408142), Vector3(0.8660253882408142, 0.5, -0.0),
            Vector3(0.8660253882408142, -0.5, 0.0), Vector3(-0.0, -0.5, 0.8660253882408142),
            Vector3(1.8660253882408142, -0.5, 0.0), Vector3(0.0, -0.5, -0.8660253882408142),
            Vector3(0.8660253882408142, -0.5, -0.0), Vector3(-0.0, -1.0, 0.0),
            Vector3(-0.0, -1.0, 0.0), Vector3(0.0, -1.0, 0.0),
            Vector3(0.0, -1.0, -0.0), Vector3(-0.0, -1.0, -0.0),
        )

        a.setFromPoints(points)
        assertTrue(abs(a.center.x - expectedCenter.x) <= eps, "Default center: check center.x")
        assertTrue(abs(a.center.y - expectedCenter.y) <= eps, "Default center: check center.y")
        assertTrue(abs(a.center.z - expectedCenter.z) <= eps, "Default center: check center.z")
        assertTrue(abs(a.radius - expectedRadius) <= eps, "Default center: check radius")

        expectedRadius = 2.5946195770400102
        a.setFromPoints(points, optionalCenter)
        assertTrue(abs(a.center.x - optionalCenter.x) <= eps, "Optional center: check center.x")
        assertTrue(abs(a.center.y - optionalCenter.y) <= eps, "Optional center: check center.y")
        assertTrue(abs(a.center.z - optionalCenter.z) <= eps, "Optional center: check center.z")
        assertTrue(abs(a.radius - expectedRadius) <= eps, "Optional center: check radius")
    }

    @Test
    fun copy() {
        val a = Sphere(one3.clone(), 1.0)
        val b = Sphere().copy(a)

        assertTrue(b.center == one3)
        assertTrue(b.radius == 1.0)

        // ensure that it is a true copy.
        // Upstream reassigns a.center (fresh object); here center is `val`, so we
        // mutate a's contents in place instead. The intent is the same:
        // b must be unaffected by later changes to a.
        a.center.set(0.0, 0.0, 0.0)
        a.radius = 0.0
        assertTrue(b.center == one3)
        assertTrue(b.radius == 1.0)
    }

    @Test
    fun isEmpty() {
        val a = Sphere()
        assertTrue(a.isEmpty())

        a.set(one3, 1.0)
        assertFalse(a.isEmpty())

        // Negative radius contains no points
        a.set(one3, -1.0)
        assertTrue(a.isEmpty())

        // Zero radius contains only the center point
        a.set(one3, 0.0)
        assertFalse(a.isEmpty())
    }

    @Test
    fun makeEmpty() {
        val a = Sphere(one3.clone(), 1.0)

        assertFalse(a.isEmpty())

        a.makeEmpty()
        assertTrue(a.isEmpty())
        assertTrue(a.center == zero3)
    }

    @Test
    fun containsPoint() {
        val a = Sphere(one3.clone(), 1.0)

        assertFalse(a.containsPoint(zero3))
        assertTrue(a.containsPoint(one3))

        a.set(zero3, 0.0)
        assertTrue(a.containsPoint(a.center))
    }

    @Test
    fun distanceToPoint() {
        val a = Sphere(one3.clone(), 1.0)

        assertTrue((a.distanceToPoint(zero3) - 0.7320) < 0.001)
        assertTrue(a.distanceToPoint(one3) == -1.0)
    }

    @Test
    fun intersectsSphere() {
        val a = Sphere(one3.clone(), 1.0)
        val b = Sphere(zero3.clone(), 1.0)
        val c = Sphere(zero3.clone(), 0.25)

        assertTrue(a.intersectsSphere(b))
        assertFalse(a.intersectsSphere(c))
    }

    @Test
    fun intersectsBox() {
        val a = Sphere(zero3, 1.0)
        val b = Sphere(Vector3(-5.0, -5.0, -5.0), 1.0)
        val box = Box3(zero3, one3)

        assertEquals(true, a.intersectsBox(box), "Check unit sphere")
        assertEquals(false, b.intersectsBox(box), "Check shifted sphere")
    }

    @Test
    fun intersectsPlane() {
        val a = Sphere(zero3.clone(), 1.0)
        val b = Plane(Vector3(0.0, 1.0, 0.0), 1.0)
        val c = Plane(Vector3(0.0, 1.0, 0.0), 1.25)
        val d = Plane(Vector3(0.0, -1.0, 0.0), 1.25)

        assertTrue(a.intersectsPlane(b))
        assertFalse(a.intersectsPlane(c))
        assertFalse(a.intersectsPlane(d))
    }

    @Test
    fun clampPoint() {
        val a = Sphere(one3.clone(), 1.0)
        val point = Vector3()

        a.clampPoint(Vector3(1.0, 1.0, 3.0), point)
        assertTrue(point == Vector3(1.0, 1.0, 2.0))
        a.clampPoint(Vector3(1.0, 1.0, -3.0), point)
        assertTrue(point == Vector3(1.0, 1.0, 0.0))
    }

    @Test
    fun getBoundingBox() {
        val a = Sphere(one3.clone(), 1.0)
        val aabb = Box3()

        a.getBoundingBox(aabb)
        assertTrue(aabb == Box3(zero3, two3))

        a.set(zero3, 0.0)
        a.getBoundingBox(aabb)
        assertTrue(aabb == Box3(zero3, zero3))

        // Empty sphere produces empty bounding box
        a.makeEmpty()
        a.getBoundingBox(aabb)
        assertTrue(aabb.isEmpty())
    }

    @Test
    fun applyMatrix4() {
        val a = Sphere(one3.clone(), 1.0)
        val m = Matrix4().makeTranslation(1.0, -2.0, 1.0)
        val aabb1 = Box3()
        val aabb2 = Box3()

        a.clone().applyMatrix4(m).getBoundingBox(aabb1)
        a.getBoundingBox(aabb2)

        assertTrue(aabb1 == aabb2.applyMatrix4(m))
    }

    @Test
    fun translate() {
        val a = Sphere(one3.clone(), 1.0)

        a.translate(one3.clone().negate())
        assertTrue(a.center == zero3)
    }

    @Test
    fun expandByPoint() {
        val a = Sphere(zero3.clone(), 1.0)
        val p = Vector3(2.0, 0.0, 0.0)

        assertFalse(a.containsPoint(p), "a does not contain p")

        a.expandByPoint(p)

        assertTrue(a.containsPoint(p), "a does contain p")
        assertTrue(a.center == Vector3(0.5, 0.0, 0.0))
        assertTrue(a.radius == 1.5)
    }

    @Test
    fun union() {
        val a = Sphere(zero3.clone(), 1.0)
        val b = Sphere(Vector3(2.0, 0.0, 0.0), 1.0)

        a.union(b)

        assertTrue(a.center == Vector3(1.0, 0.0, 0.0))
        assertTrue(a.radius == 2.0)

        // d contains c (demonstrates why it is necessary to process two points in union)

        val c = Sphere(Vector3(), 1.0)
        val d = Sphere(Vector3(1.0, 0.0, 0.0), 4.0)

        c.union(d)

        assertTrue(c.center == Vector3(1.0, 0.0, 0.0))
        assertTrue(c.radius == 4.0)

        // edge case: both spheres have the same center point

        val e = Sphere(Vector3(), 1.0)
        val f = Sphere(Vector3(), 4.0)

        e.union(f)

        assertTrue(e.center == Vector3(0.0, 0.0, 0.0))
        assertTrue(e.radius == 4.0)
    }

    @Test
    fun equalsTest() {
        val a = Sphere()
        val b = Sphere(Vector3(1.0, 0.0, 0.0))
        val c = Sphere(Vector3(1.0, 0.0, 0.0), 1.0)

        assertEquals(false, a == b, "a does not equal b")
        assertEquals(false, a == c, "a does not equal c")
        assertEquals(false, b == c, "b does not equal c")

        a.copy(b)
        assertEquals(true, a == b, "a equals b after copy()")
    }

    // Upstream Sphere.tests.js has no toJSON/fromJSON case; this one is written
    // fresh to cover the flattened DoubleArray round-trip the port exposes.
    @Test
    fun toJSONFromJSON() {
        val a = Sphere(Vector3(1.0, -2.0, 3.0), 4.0)

        assertContentEquals(doubleArrayOf(1.0, -2.0, 3.0, 4.0), a.toJSON(), "toJSON is [cx, cy, cz, radius]")

        val b = Sphere().fromJSON(a.toJSON())
        assertEquals(a, b, "fromJSON(toJSON()) round-trips")
    }
}
