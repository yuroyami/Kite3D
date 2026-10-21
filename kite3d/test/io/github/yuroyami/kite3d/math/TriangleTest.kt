/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Triangle.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TriangleTest {

    @Test
    fun instancing() {
        var a = Triangle()
        assertTrue(a.a == zero3)
        assertTrue(a.b == zero3)
        assertTrue(a.c == zero3)

        a = Triangle(one3.clone().negate(), one3.clone(), two3.clone())
        assertTrue(a.a == one3.clone().negate())
        assertTrue(a.b == one3)
        assertTrue(a.c == two3)
    }

    @Test
    fun set() {
        val a = Triangle()

        a.set(one3.clone().negate(), one3, two3)
        assertTrue(a.a == one3.clone().negate())
        assertTrue(a.b == one3)
        assertTrue(a.c == two3)
    }

    @Test
    fun setFromPointsAndIndices() {
        val a = Triangle()

        val points = listOf(one3, one3.clone().negate(), two3)
        a.setFromPointsAndIndices(points, 1, 0, 2)
        assertTrue(a.a == one3.clone().negate())
        assertTrue(a.b == one3)
        assertTrue(a.c == two3)
    }

    @Test
    fun setFromAttributeAndIndices() {
        val a = Triangle()
        val attribute = TestBufferAttribute(
            doubleArrayOf(1.0, 1.0, 1.0, -1.0, -1.0, -1.0, 2.0, 2.0, 2.0),
            3,
        )

        a.setFromAttributeAndIndices(attribute, 1, 0, 2)
        assertTrue(a.a == one3.clone().negate())
        assertTrue(a.b == one3)
        assertTrue(a.c == two3)
    }

    @Test
    fun copy() {
        val a = Triangle(one3.clone().negate(), one3.clone(), two3.clone())
        val b = Triangle().copy(a)
        assertTrue(b.a == one3.clone().negate())
        assertTrue(b.b == one3)
        assertTrue(b.c == two3)

        // ensure that it is a true copy
        a.a = one3
        a.b = zero3
        a.c = zero3
        assertTrue(b.a == one3.clone().negate())
        assertTrue(b.b == one3)
        assertTrue(b.c == two3)
    }

    @Test
    fun getArea() {
        // Algebraic (only + - * sqrt of perfect squares) → exact.
        var a = Triangle()

        assertEquals(0.0, a.getArea(), 0.0)

        a = Triangle(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0))
        assertEquals(0.5, a.getArea(), 0.0)

        a = Triangle(Vector3(2.0, 0.0, 0.0), Vector3(0.0, 0.0, 0.0), Vector3(0.0, 0.0, 2.0))
        assertEquals(2.0, a.getArea(), 0.0)

        // colinear triangle.
        a = Triangle(Vector3(2.0, 0.0, 0.0), Vector3(0.0, 0.0, 0.0), Vector3(3.0, 0.0, 0.0))
        assertEquals(0.0, a.getArea(), 0.0)
    }

    @Test
    fun getMidpoint() {
        var a = Triangle()
        val midpoint = Vector3()

        assertTrue(a.getMidpoint(midpoint) == Vector3(0.0, 0.0, 0.0))

        a = Triangle(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0))
        assertTrue(a.getMidpoint(midpoint) == Vector3(1.0 / 3.0, 1.0 / 3.0, 0.0))

        a = Triangle(Vector3(2.0, 0.0, 0.0), Vector3(0.0, 0.0, 0.0), Vector3(0.0, 0.0, 2.0))
        assertTrue(a.getMidpoint(midpoint) == Vector3(2.0 / 3.0, 0.0, 2.0 / 3.0))
    }

    @Test
    fun getNormal() {
        var a = Triangle()
        val normal = Vector3()

        assertTrue(a.getNormal(normal) == Vector3(0.0, 0.0, 0.0))

        a = Triangle(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0))
        assertTrue(a.getNormal(normal) == Vector3(0.0, 0.0, 1.0))

        a = Triangle(Vector3(2.0, 0.0, 0.0), Vector3(0.0, 0.0, 0.0), Vector3(0.0, 0.0, 2.0))
        assertTrue(a.getNormal(normal) == Vector3(0.0, 1.0, 0.0))
    }

    @Test
    fun getPlane() {
        var a = Triangle()
        val plane = Plane()
        val normal = Vector3()

        a.getPlane(plane)
        assertFalse(plane.distanceToPoint(a.a).isNaN())
        assertFalse(plane.distanceToPoint(a.b).isNaN())
        assertFalse(plane.distanceToPoint(a.c).isNaN())
        // three.js's notPropEqual(plane.normal, {x:NaN,y:NaN,z:NaN}); the normal must
        // not be all-NaN. Assert each component is a real number instead.
        assertFalse(plane.normal.x.isNaN() && plane.normal.y.isNaN() && plane.normal.z.isNaN())

        a = Triangle(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0))
        a.getPlane(plane)
        a.getNormal(normal)
        assertEquals(0.0, plane.distanceToPoint(a.a), 0.0)
        assertEquals(0.0, plane.distanceToPoint(a.b), 0.0)
        assertEquals(0.0, plane.distanceToPoint(a.c), 0.0)
        assertTrue(plane.normal == normal)

        a = Triangle(Vector3(2.0, 0.0, 0.0), Vector3(0.0, 0.0, 0.0), Vector3(0.0, 0.0, 2.0))
        a.getPlane(plane)
        a.getNormal(normal)
        assertEquals(0.0, plane.distanceToPoint(a.a), 0.0)
        assertEquals(0.0, plane.distanceToPoint(a.b), 0.0)
        assertEquals(0.0, plane.distanceToPoint(a.c), 0.0)
        assertTrue(plane.normal.clone().normalize() == normal)
    }

    @Test
    fun getBarycoord() {
        var a = Triangle()

        val barycoord = Vector3()
        val midpoint = Vector3()

        assertNull(a.getBarycoord(a.a, barycoord))
        assertNull(a.getBarycoord(a.b, barycoord))
        assertNull(a.getBarycoord(a.c, barycoord))

        a = Triangle(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0))
        a.getMidpoint(midpoint)

        a.getBarycoord(a.a, barycoord)
        assertTrue(barycoord == Vector3(1.0, 0.0, 0.0))
        a.getBarycoord(a.b, barycoord)
        assertTrue(barycoord == Vector3(0.0, 1.0, 0.0))
        a.getBarycoord(a.c, barycoord)
        assertTrue(barycoord == Vector3(0.0, 0.0, 1.0))
        a.getBarycoord(midpoint, barycoord)
        assertTrue(barycoord.distanceTo(Vector3(1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0)) < 0.0001)

        a = Triangle(Vector3(2.0, 0.0, 0.0), Vector3(0.0, 0.0, 0.0), Vector3(0.0, 0.0, 2.0))
        a.getMidpoint(midpoint)

        a.getBarycoord(a.a, barycoord)
        assertTrue(barycoord == Vector3(1.0, 0.0, 0.0))
        a.getBarycoord(a.b, barycoord)
        assertTrue(barycoord == Vector3(0.0, 1.0, 0.0))
        a.getBarycoord(a.c, barycoord)
        assertTrue(barycoord == Vector3(0.0, 0.0, 1.0))
        a.getBarycoord(midpoint, barycoord)
        assertTrue(barycoord.distanceTo(Vector3(1.0 / 3.0, 1.0 / 3.0, 1.0 / 3.0)) < 0.0001)
    }

    @Test
    fun containsPoint() {
        var a = Triangle()
        val midpoint = Vector3()

        assertFalse(a.containsPoint(a.a))
        assertFalse(a.containsPoint(a.b))
        assertFalse(a.containsPoint(a.c))

        a = Triangle(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0))
        a.getMidpoint(midpoint)
        assertTrue(a.containsPoint(a.a))
        assertTrue(a.containsPoint(a.b))
        assertTrue(a.containsPoint(a.c))
        assertTrue(a.containsPoint(midpoint))
        assertFalse(a.containsPoint(Vector3(-1.0, -1.0, -1.0)))

        a = Triangle(Vector3(2.0, 0.0, 0.0), Vector3(0.0, 0.0, 0.0), Vector3(0.0, 0.0, 2.0))
        a.getMidpoint(midpoint)
        assertTrue(a.containsPoint(a.a))
        assertTrue(a.containsPoint(a.b))
        assertTrue(a.containsPoint(a.c))
        assertTrue(a.containsPoint(midpoint))
        assertFalse(a.containsPoint(Vector3(-1.0, -1.0, -1.0)))
    }

    @Test
    fun intersectsBox() {
        val a = Box3(one3.clone(), two3.clone())
        val b = Triangle(Vector3(1.5, 1.5, 2.5), Vector3(2.5, 1.5, 1.5), Vector3(1.5, 2.5, 1.5))
        val c = Triangle(Vector3(1.5, 1.5, 3.5), Vector3(3.5, 1.5, 1.5), Vector3(1.5, 1.5, 1.5))
        val d = Triangle(Vector3(1.5, 1.75, 3.0), Vector3(3.0, 1.75, 1.5), Vector3(1.5, 2.5, 1.5))
        val e = Triangle(Vector3(1.5, 1.8, 3.0), Vector3(3.0, 1.8, 1.5), Vector3(1.5, 2.5, 1.5))
        val f = Triangle(Vector3(1.5, 2.5, 3.0), Vector3(3.0, 2.5, 1.5), Vector3(1.5, 2.5, 1.5))

        assertTrue(b.intersectsBox(a))
        assertTrue(c.intersectsBox(a))
        assertTrue(d.intersectsBox(a))
        assertFalse(e.intersectsBox(a))
        assertFalse(f.intersectsBox(a))
    }

    @Test
    fun closestPointToPoint() {
        val a = Triangle(Vector3(-1.0, 0.0, 0.0), Vector3(1.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0))
        val point = Vector3()

        // point lies inside the triangle
        a.closestPointToPoint(Vector3(0.0, 0.5, 0.0), point)
        assertTrue(point == Vector3(0.0, 0.5, 0.0))

        // point lies on a vertex
        a.closestPointToPoint(a.a, point)
        assertTrue(point == a.a)

        a.closestPointToPoint(a.b, point)
        assertTrue(point == a.b)

        a.closestPointToPoint(a.c, point)
        assertTrue(point == a.c)

        // point lies on an edge
        a.closestPointToPoint(zero3.clone(), point)
        assertTrue(point == zero3.clone())

        // point lies outside the triangle
        a.closestPointToPoint(Vector3(-2.0, 0.0, 0.0), point)
        assertTrue(point == Vector3(-1.0, 0.0, 0.0))

        a.closestPointToPoint(Vector3(2.0, 0.0, 0.0), point)
        assertTrue(point == Vector3(1.0, 0.0, 0.0))

        a.closestPointToPoint(Vector3(0.0, 2.0, 0.0), point)
        assertTrue(point == Vector3(0.0, 1.0, 0.0))

        a.closestPointToPoint(Vector3(0.0, -2.0, 0.0), point)
        assertTrue(point == Vector3(0.0, 0.0, 0.0))
    }

    @Test
    fun isFrontFacing() {
        var a = Triangle()
        var dir = Vector3()
        assertFalse(a.isFrontFacing(dir))

        a = Triangle(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0))
        dir = Vector3(0.0, 0.0, -1.0)
        assertTrue(a.isFrontFacing(dir))

        a = Triangle(Vector3(0.0, 0.0, 0.0), Vector3(0.0, 1.0, 0.0), Vector3(1.0, 0.0, 0.0))
        assertFalse(a.isFrontFacing(dir))
    }

    @Test
    fun equalsTest() {
        val a = Triangle(
            Vector3(1.0, 0.0, 0.0),
            Vector3(0.0, 1.0, 0.0),
            Vector3(0.0, 0.0, 1.0),
        )
        val b = Triangle(
            Vector3(0.0, 0.0, 1.0),
            Vector3(0.0, 1.0, 0.0),
            Vector3(1.0, 0.0, 0.0),
        )
        val c = Triangle(
            Vector3(-1.0, 0.0, 0.0),
            Vector3(0.0, 1.0, 0.0),
            Vector3(0.0, 0.0, 1.0),
        )

        assertTrue(a == a)
        assertFalse(a == b)
        assertFalse(a == c)
        assertFalse(b == c)

        a.copy(b)
        assertTrue(a == a)
    }
}
