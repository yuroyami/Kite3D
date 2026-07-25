/*
 * Copyright © 2026 yuroyami. MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Box2.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Box2Test {

    @Test
    fun instancing() {
        var a = Box2()
        assertTrue(a.min == posInf2)
        assertTrue(a.max == negInf2)

        a = Box2(zero2, zero2)
        assertTrue(a.min == zero2)
        assertTrue(a.max == zero2)

        a = Box2(zero2, one2)
        assertTrue(a.min == zero2)
        assertTrue(a.max == one2)
    }

    // three.js's `isBox2` duck-typing flag is intentionally dropped (dialect rule
    // 11): type identity is expressed with Kotlin's `is Box2`, which needs no test.

    @Test
    fun set() {
        val a = Box2()
        a.set(zero2, one2)
        assertTrue(a.min == zero2)
        assertTrue(a.max == one2)
    }

    @Test
    fun setFromPoints() {
        val a = Box2()

        a.setFromPoints(listOf(zero2, one2, two2))
        assertTrue(a.min == zero2)
        assertTrue(a.max == two2)

        a.setFromPoints(listOf(one2))
        assertTrue(a.min == one2)
        assertTrue(a.max == one2)

        a.setFromPoints(emptyList())
        assertTrue(a.isEmpty())
    }

    @Test
    fun setFromCenterAndSize() {
        val a = Box2()

        a.setFromCenterAndSize(zero2, two2)
        assertTrue(a.min == negOne2)
        assertTrue(a.max == one2)

        a.setFromCenterAndSize(one2, two2)
        assertTrue(a.min == zero2)
        assertTrue(a.max == two2)

        a.setFromCenterAndSize(zero2, zero2)
        assertTrue(a.min == zero2)
        assertTrue(a.max == zero2)
    }

    @Test
    fun clone() {
        var a = Box2(zero2, zero2)
        var b = a.clone()
        assertTrue(b.min == zero2)
        assertTrue(b.max == zero2)

        a = Box2()
        b = a.clone()
        assertTrue(b.min == posInf2)
        assertTrue(b.max == negInf2)
    }

    @Test
    fun copy() {
        val a = Box2(zero2, one2)
        val b = Box2().copy(a)
        assertTrue(b.min == zero2)
        assertTrue(b.max == one2)

        // ensure it is a true copy: mutating a's bounds must not affect b
        a.min.set(5.0, 6.0)
        a.max.set(7.0, 8.0)
        assertTrue(b.min == zero2)
        assertTrue(b.max == one2)
    }

    @Test
    fun emptyMakeEmpty() {
        var a = Box2()
        assertTrue(a.isEmpty())

        a = Box2(zero2, one2)
        assertFalse(a.isEmpty())

        a.makeEmpty()
        assertTrue(a.isEmpty())
    }

    @Test
    fun isEmpty() {
        var a = Box2(zero2, zero2)
        assertFalse(a.isEmpty())

        a = Box2(zero2, one2)
        assertFalse(a.isEmpty())

        a = Box2(two2, one2)
        assertTrue(a.isEmpty())

        a = Box2(posInf2, negInf2)
        assertTrue(a.isEmpty())
    }

    @Test
    fun getCenter() {
        var a = Box2(zero2, zero2)
        val center = Vector2()
        assertTrue(a.getCenter(center) == zero2)

        a = Box2(zero2, one2)
        val midpoint = one2.multiplyScalar(0.5)
        assertTrue(a.getCenter(center) == midpoint)
    }

    @Test
    fun getSize() {
        var a = Box2(zero2, zero2)
        val size = Vector2()

        assertTrue(a.getSize(size) == zero2)

        a = Box2(zero2, one2)
        assertTrue(a.getSize(size) == one2)
    }

    @Test
    fun expandByPoint() {
        val a = Box2(zero2, zero2)
        val size = Vector2()
        val center = Vector2()

        a.expandByPoint(zero2)
        assertTrue(a.getSize(size) == zero2)

        a.expandByPoint(one2)
        assertTrue(a.getSize(size) == one2)

        a.expandByPoint(one2.clone().negate())
        assertTrue(a.getSize(size) == one2.clone().multiplyScalar(2.0))
        assertTrue(a.getCenter(center) == zero2)
    }

    @Test
    fun expandByVector() {
        val a = Box2(zero2, zero2)
        val size = Vector2()
        val center = Vector2()

        a.expandByVector(zero2)
        assertTrue(a.getSize(size) == zero2)

        a.expandByVector(one2)
        assertTrue(a.getSize(size) == one2.clone().multiplyScalar(2.0))
        assertTrue(a.getCenter(center) == zero2)
    }

    @Test
    fun expandByScalar() {
        val a = Box2(zero2, zero2)
        val size = Vector2()
        val center = Vector2()

        a.expandByScalar(0.0)
        assertTrue(a.getSize(size) == zero2)

        a.expandByScalar(1.0)
        assertTrue(a.getSize(size) == one2.clone().multiplyScalar(2.0))
        assertTrue(a.getCenter(center) == zero2)
    }

    @Test
    fun containsPoint() {
        val a = Box2(zero2, zero2)

        assertTrue(a.containsPoint(zero2))
        assertFalse(a.containsPoint(one2))

        a.expandByScalar(1.0)
        assertTrue(a.containsPoint(zero2))
        assertTrue(a.containsPoint(one2))
        assertTrue(a.containsPoint(one2.clone().negate()))
    }

    @Test
    fun containsBox() {
        val a = Box2(zero2, zero2)
        val b = Box2(zero2, one2)
        val c = Box2(one2.clone().negate(), one2)

        assertTrue(a.containsBox(a))
        assertFalse(a.containsBox(b))
        assertFalse(a.containsBox(c))

        assertTrue(b.containsBox(a))
        assertTrue(c.containsBox(a))
        assertFalse(b.containsBox(c))
    }

    @Test
    fun getParameter() {
        val a = Box2(zero2, one2)
        val b = Box2(one2.clone().negate(), one2)

        val parameter = Vector2()

        a.getParameter(zero2, parameter)
        assertTrue(parameter == zero2)
        a.getParameter(one2, parameter)
        assertTrue(parameter == one2)

        b.getParameter(one2.clone().negate(), parameter)
        assertTrue(parameter == zero2)
        b.getParameter(zero2, parameter)
        assertTrue(parameter == Vector2(0.5, 0.5))
        b.getParameter(one2, parameter)
        assertTrue(parameter == one2)
    }

    @Test
    fun intersectsBox() {
        val a = Box2(zero2, zero2)
        val b = Box2(zero2, one2)
        val c = Box2(one2.clone().negate(), one2)

        assertTrue(a.intersectsBox(a))
        assertTrue(a.intersectsBox(b))
        assertTrue(a.intersectsBox(c))

        assertTrue(b.intersectsBox(a))
        assertTrue(c.intersectsBox(a))
        assertTrue(b.intersectsBox(c))

        b.translate(two2)
        assertFalse(a.intersectsBox(b))
        assertFalse(b.intersectsBox(a))
        assertFalse(b.intersectsBox(c))
    }

    @Test
    fun clampPoint() {
        val a = Box2(zero2, zero2)
        val b = Box2(one2.clone().negate(), one2)

        val point = Vector2()

        a.clampPoint(zero2, point)
        assertTrue(point == Vector2(0.0, 0.0))
        a.clampPoint(one2, point)
        assertTrue(point == Vector2(0.0, 0.0))
        a.clampPoint(one2.clone().negate(), point)
        assertTrue(point == Vector2(0.0, 0.0))

        b.clampPoint(two2, point)
        assertTrue(point == Vector2(1.0, 1.0))
        b.clampPoint(one2, point)
        assertTrue(point == Vector2(1.0, 1.0))
        b.clampPoint(zero2, point)
        assertTrue(point == Vector2(0.0, 0.0))
        b.clampPoint(one2.clone().negate(), point)
        assertTrue(point == Vector2(-1.0, -1.0))
        b.clampPoint(two2.clone().negate(), point)
        assertTrue(point == Vector2(-1.0, -1.0))
    }

    @Test
    fun distanceToPoint() {
        val a = Box2(zero2, zero2)
        val b = Box2(one2.clone().negate(), one2)

        assertEquals(0.0, a.distanceToPoint(Vector2(0.0, 0.0)))
        assertEquals(sqrt(2.0), a.distanceToPoint(Vector2(1.0, 1.0)))
        assertEquals(sqrt(2.0), a.distanceToPoint(Vector2(-1.0, -1.0)))

        assertEquals(sqrt(2.0), b.distanceToPoint(Vector2(2.0, 2.0)))
        assertEquals(0.0, b.distanceToPoint(Vector2(1.0, 1.0)))
        assertEquals(0.0, b.distanceToPoint(Vector2(0.0, 0.0)))
        assertEquals(0.0, b.distanceToPoint(Vector2(-1.0, -1.0)))
        assertEquals(sqrt(2.0), b.distanceToPoint(Vector2(-2.0, -2.0)))
    }

    @Test
    fun intersect() {
        val a = Box2(zero2, zero2)
        val b = Box2(zero2, one2)
        val c = Box2(one2.clone().negate(), one2)

        assertTrue(a.clone().intersect(a) == a)
        assertTrue(a.clone().intersect(b) == a)
        assertTrue(b.clone().intersect(b) == b)
        assertTrue(a.clone().intersect(c) == a)
        assertTrue(b.clone().intersect(c) == b)
        assertTrue(c.clone().intersect(c) == c)

        val d = Box2(one2.clone().negate(), zero2)
        val e = Box2(one2, two2).intersect(d)

        assertTrue(e.min == posInf2 && e.max == negInf2, "Infinite empty")
    }

    @Test
    fun union() {
        val a = Box2(zero2, zero2)
        val b = Box2(zero2, one2)
        val c = Box2(one2.clone().negate(), one2)

        assertTrue(a.clone().union(a) == a)
        assertTrue(a.clone().union(b) == b)
        assertTrue(a.clone().union(c) == c)
        assertTrue(b.clone().union(c) == c)
    }

    @Test
    fun translate() {
        val a = Box2(zero2, zero2)
        val b = Box2(zero2, one2)
        val c = Box2(one2.clone().negate(), zero2)

        assertTrue(a.clone().translate(one2) == Box2(one2, one2))
        assertTrue(a.clone().translate(one2).translate(one2.clone().negate()) == a)
        assertTrue(c.clone().translate(one2) == b)
        assertTrue(b.clone().translate(one2.clone().negate()) == c)
    }

    @Test
    fun equalsTest() {
        var a = Box2()
        var b = Box2()
        assertTrue(b == a)
        assertTrue(a == b)

        a = Box2(one2, two2)
        b = Box2(one2, two2)
        assertTrue(b == a)
        assertTrue(a == b)

        a = Box2(one2, two2)
        b = a.clone()
        assertTrue(b == a)
        assertTrue(a == b)

        a = Box2(one2, two2)
        b = Box2(one2, one2)
        assertFalse(b == a)
        assertFalse(a == b)

        a = Box2()
        b = Box2(one2, one2)
        assertFalse(b == a)
        assertFalse(a == b)
    }
}
