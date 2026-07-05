/*
 * Copyright © 2026 yuroyami — MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Box3.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Box3Test {

    // Mirrors the JS `compareBox` helper (min/max within a small threshold).
    private fun compareBox(a: Box3, b: Box3, threshold: Double = 0.0001): Boolean =
        a.min.distanceTo(b.min) < threshold && a.max.distanceTo(b.max) < threshold

    @Test
    fun instancing() {
        var a = Box3()
        assertTrue(a.min == posInf3)
        assertTrue(a.max == negInf3)

        a = Box3(zero3, zero3)
        assertTrue(a.min == zero3)
        assertTrue(a.max == zero3)

        a = Box3(zero3, one3)
        assertTrue(a.min == zero3)
        assertTrue(a.max == one3)
    }

    // three.js's `isBox3` duck-typing flag is intentionally dropped (dialect rule
    // 11): type identity is expressed with Kotlin's `is Box3`, which needs no test.

    @Test
    fun set() {
        val a = Box3()

        a.set(zero3, one3)
        assertTrue(a.min == zero3)
        assertTrue(a.max == one3)
    }

    @Test
    fun setFromArray() {
        val a = Box3()

        a.setFromArray(doubleArrayOf(0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0))
        assertTrue(a.min == zero3)
        assertTrue(a.max == two3)
    }

    @Test
    fun setFromBufferAttribute() {
        // three.js passes a BufferAttribute and reads attribute.count; the
        // AttributeLike seam has no count, so the vertex count is passed explicitly
        // (see Box3.setFromBufferAttribute KDoc). Float32Array in JS holds these
        // exact values, so the DoubleArray-backed test double reproduces them.
        val a = Box3(zero3, one3)
        val bigger = TestBufferAttribute(
            doubleArrayOf(-2.0, -2.0, -2.0, 2.0, 2.0, 2.0, 1.5, 1.5, 1.5, 0.0, 0.0, 0.0),
            3,
        )
        val smaller = TestBufferAttribute(
            doubleArrayOf(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5, 0.0, 0.0, 0.0),
            3,
        )
        val newMin = Vector3(-2.0, -2.0, -2.0)
        val newMax = Vector3(2.0, 2.0, 2.0)

        a.setFromBufferAttribute(bigger, 4)
        assertTrue(a.min == newMin, "Bigger box: correct new minimum")
        assertTrue(a.max == newMax, "Bigger box: correct new maximum")

        newMin.set(-0.5, -0.5, -0.5)
        newMax.set(0.5, 0.5, 0.5)

        a.setFromBufferAttribute(smaller, 3)
        assertTrue(a.min == newMin, "Smaller box: correct new minimum")
        assertTrue(a.max == newMax, "Smaller box: correct new maximum")
    }

    @Test
    fun setFromPoints() {
        val a = Box3()

        a.setFromPoints(listOf(zero3, one3, two3))
        assertTrue(a.min == zero3)
        assertTrue(a.max == two3)

        a.setFromPoints(listOf(one3))
        assertTrue(a.min == one3)
        assertTrue(a.max == one3)

        a.setFromPoints(emptyList())
        assertTrue(a.isEmpty())
    }

    @Test
    fun setFromCenterAndSize() {
        val a = Box3(zero3, one3)
        val b = a.clone()
        val centerA = Vector3()
        val sizeA = Vector3()
        val sizeB = Vector3()
        val newCenter = one3
        val newSize = two3

        a.getCenter(centerA)
        a.getSize(sizeA)
        a.setFromCenterAndSize(centerA, sizeA)
        assertTrue(a == b, "Same values: no changes")

        a.setFromCenterAndSize(newCenter, sizeA)
        a.getCenter(centerA)
        a.getSize(sizeA)
        b.getSize(sizeB)

        assertTrue(centerA == newCenter, "Move center: correct new center")
        assertTrue(sizeA == sizeB, "Move center: no change in size")
        assertFalse(a == b, "Move center: no longer equal to old values")

        a.setFromCenterAndSize(centerA, newSize)
        a.getCenter(centerA)
        a.getSize(sizeA)
        assertTrue(centerA == newCenter, "Resize: no change to center")
        assertTrue(sizeA == newSize, "Resize: correct new size")
        assertFalse(a == b, "Resize: no longer equal to old values")
    }

    // setFromObject/BufferGeometry, setFromObject/Precise and expandByObject are
    // SKIPPED: Box3.setFromObject/expandByObject are deferred (they need core
    // Object3D/Mesh/geometry types — updateWorldMatrix, geometry, getVertexPosition,
    // matrixWorld). They will be covered once the object-aware module lands.

    @Test
    fun clone() {
        var a = Box3(zero3, one3)

        var b = a.clone()
        assertTrue(b.min == zero3)
        assertTrue(b.max == one3)

        a = Box3()
        b = a.clone()
        assertTrue(b.min == posInf3)
        assertTrue(b.max == negInf3)
    }

    @Test
    fun copy() {
        val a = Box3(zero3, one3)
        val b = Box3().copy(a)
        assertTrue(b.min == zero3)
        assertTrue(b.max == one3)

        // ensure it is a true copy: mutating a's bounds must not affect b.
        // (three.js reassigns a.min/a.max; here min/max are `val`, so mutate them.)
        a.min.set(2.0, 2.0, 2.0)
        a.max.set(3.0, 3.0, 3.0)
        assertTrue(b.min == zero3)
        assertTrue(b.max == one3)
    }

    @Test
    fun emptyMakeEmpty() {
        var a = Box3()

        assertTrue(a.isEmpty())

        a = Box3(zero3, one3)
        assertFalse(a.isEmpty())

        a.makeEmpty()
        assertTrue(a.isEmpty())
    }

    @Test
    fun isEmpty() {
        var a = Box3(zero3, zero3)
        assertFalse(a.isEmpty())

        a = Box3(zero3, one3)
        assertFalse(a.isEmpty())

        a = Box3(two3, one3)
        assertTrue(a.isEmpty())

        a = Box3(posInf3, negInf3)
        assertTrue(a.isEmpty())
    }

    @Test
    fun getCenter() {
        var a = Box3(zero3, zero3)
        val center = Vector3()

        assertTrue(a.getCenter(center) == zero3)

        a = Box3(zero3, one3)
        val midpoint = one3.multiplyScalar(0.5)
        assertTrue(a.getCenter(center) == midpoint)
    }

    @Test
    fun getSize() {
        var a = Box3(zero3, zero3)
        val size = Vector3()

        assertTrue(a.getSize(size) == zero3)

        a = Box3(zero3, one3)
        assertTrue(a.getSize(size) == one3)
    }

    @Test
    fun expandByPoint() {
        val a = Box3(zero3, zero3)
        val center = Vector3()
        val size = Vector3()

        a.expandByPoint(zero3)
        assertTrue(a.getSize(size) == zero3)

        a.expandByPoint(one3)
        assertTrue(a.getSize(size) == one3)

        a.expandByPoint(one3.clone().negate())
        assertTrue(a.getSize(size) == one3.clone().multiplyScalar(2.0))
        assertTrue(a.getCenter(center) == zero3)
    }

    @Test
    fun expandByVector() {
        val a = Box3(zero3, zero3)
        val center = Vector3()
        val size = Vector3()

        a.expandByVector(zero3)
        assertTrue(a.getSize(size) == zero3)

        a.expandByVector(one3)
        assertTrue(a.getSize(size) == one3.clone().multiplyScalar(2.0))
        assertTrue(a.getCenter(center) == zero3)
    }

    @Test
    fun expandByScalar() {
        val a = Box3(zero3, zero3)
        val center = Vector3()
        val size = Vector3()

        a.expandByScalar(0.0)
        assertTrue(a.getSize(size) == zero3)

        a.expandByScalar(1.0)
        assertTrue(a.getSize(size) == one3.clone().multiplyScalar(2.0))
        assertTrue(a.getCenter(center) == zero3)
    }

    @Test
    fun containsPoint() {
        val a = Box3(zero3, zero3)

        assertTrue(a.containsPoint(zero3))
        assertFalse(a.containsPoint(one3))

        a.expandByScalar(1.0)
        assertTrue(a.containsPoint(zero3))
        assertTrue(a.containsPoint(one3))
        assertTrue(a.containsPoint(one3.clone().negate()))
    }

    @Test
    fun containsBox() {
        val a = Box3(zero3, zero3)
        val b = Box3(zero3, one3)
        val c = Box3(one3.clone().negate(), one3)

        assertTrue(a.containsBox(a))
        assertFalse(a.containsBox(b))
        assertFalse(a.containsBox(c))

        assertTrue(b.containsBox(a))
        assertTrue(c.containsBox(a))
        assertFalse(b.containsBox(c))
    }

    @Test
    fun getParameter() {
        val a = Box3(zero3, one3)
        val b = Box3(one3.clone().negate(), one3)
        val parameter = Vector3()

        a.getParameter(zero3, parameter)
        assertTrue(parameter == zero3)
        a.getParameter(one3, parameter)
        assertTrue(parameter == one3)

        b.getParameter(one3.clone().negate(), parameter)
        assertTrue(parameter == zero3)
        b.getParameter(zero3, parameter)
        assertTrue(parameter == Vector3(0.5, 0.5, 0.5))
        b.getParameter(one3, parameter)
        assertTrue(parameter == one3)
    }

    @Test
    fun intersectsBox() {
        val a = Box3(zero3, zero3)
        val b = Box3(zero3, one3)
        val c = Box3(one3.clone().negate(), one3)

        assertTrue(a.intersectsBox(a))
        assertTrue(a.intersectsBox(b))
        assertTrue(a.intersectsBox(c))

        assertTrue(b.intersectsBox(a))
        assertTrue(c.intersectsBox(a))
        assertTrue(b.intersectsBox(c))

        b.translate(Vector3(2.0, 2.0, 2.0))
        assertFalse(a.intersectsBox(b))
        assertFalse(b.intersectsBox(a))
        assertFalse(b.intersectsBox(c))
    }

    @Test
    fun intersectsSphere() {
        val a = Box3(zero3, one3)
        val b = Sphere(zero3, 1.0)

        assertTrue(a.intersectsSphere(b))

        b.translate(Vector3(2.0, 2.0, 2.0))
        assertFalse(a.intersectsSphere(b))
    }

    @Test
    fun intersectsPlane() {
        val a = Box3(zero3, one3)
        val b = Plane(Vector3(0.0, 1.0, 0.0), 1.0)
        val c = Plane(Vector3(0.0, 1.0, 0.0), 1.25)
        val d = Plane(Vector3(0.0, -1.0, 0.0), 1.25)
        val e = Plane(Vector3(0.0, 1.0, 0.0), 0.25)
        val f = Plane(Vector3(0.0, 1.0, 0.0), -0.25)
        val g = Plane(Vector3(0.0, 1.0, 0.0), -0.75)
        val h = Plane(Vector3(0.0, 1.0, 0.0), -1.0)
        val i = Plane(Vector3(1.0, 1.0, 1.0).normalize(), -1.732)
        val j = Plane(Vector3(1.0, 1.0, 1.0).normalize(), -1.733)

        assertFalse(a.intersectsPlane(b))
        assertFalse(a.intersectsPlane(c))
        assertFalse(a.intersectsPlane(d))
        assertFalse(a.intersectsPlane(e))
        assertTrue(a.intersectsPlane(f))
        assertTrue(a.intersectsPlane(g))
        assertTrue(a.intersectsPlane(h))
        assertTrue(a.intersectsPlane(i))
        assertFalse(a.intersectsPlane(j))
    }

    @Test
    fun intersectsTriangle() {
        val a = Box3(one3, two3)
        val b = Triangle(Vector3(1.5, 1.5, 2.5), Vector3(2.5, 1.5, 1.5), Vector3(1.5, 2.5, 1.5))
        val c = Triangle(Vector3(1.5, 1.5, 3.5), Vector3(3.5, 1.5, 1.5), Vector3(1.5, 1.5, 1.5))
        val d = Triangle(Vector3(1.5, 1.75, 3.0), Vector3(3.0, 1.75, 1.5), Vector3(1.5, 2.5, 1.5))
        val e = Triangle(Vector3(1.5, 1.8, 3.0), Vector3(3.0, 1.8, 1.5), Vector3(1.5, 2.5, 1.5))
        val f = Triangle(Vector3(1.5, 2.5, 3.0), Vector3(3.0, 2.5, 1.5), Vector3(1.5, 2.5, 1.5))

        assertTrue(a.intersectsTriangle(b))
        assertTrue(a.intersectsTriangle(c))
        assertTrue(a.intersectsTriangle(d))
        assertFalse(a.intersectsTriangle(e))
        assertFalse(a.intersectsTriangle(f))
    }

    @Test
    fun clampPoint() {
        val a = Box3(zero3, zero3)
        val b = Box3(one3.clone().negate(), one3)
        val point = Vector3()

        a.clampPoint(zero3, point)
        assertTrue(point == zero3)
        a.clampPoint(one3, point)
        assertTrue(point == zero3)
        a.clampPoint(one3.clone().negate(), point)
        assertTrue(point == zero3)

        b.clampPoint(Vector3(2.0, 2.0, 2.0), point)
        assertTrue(point == one3)
        b.clampPoint(one3, point)
        assertTrue(point == one3)
        b.clampPoint(zero3, point)
        assertTrue(point == zero3)
        b.clampPoint(one3.clone().negate(), point)
        assertTrue(point == one3.clone().negate())
        b.clampPoint(Vector3(-2.0, -2.0, -2.0), point)
        assertTrue(point == one3.clone().negate())
    }

    @Test
    fun distanceToPoint() {
        val a = Box3(zero3, zero3)
        val b = Box3(one3.clone().negate(), one3)

        assertTrue(a.distanceToPoint(Vector3(0.0, 0.0, 0.0)) == 0.0)
        assertTrue(a.distanceToPoint(Vector3(1.0, 1.0, 1.0)) == sqrt(3.0))
        assertTrue(a.distanceToPoint(Vector3(-1.0, -1.0, -1.0)) == sqrt(3.0))

        assertTrue(b.distanceToPoint(Vector3(2.0, 2.0, 2.0)) == sqrt(3.0))
        assertTrue(b.distanceToPoint(Vector3(1.0, 1.0, 1.0)) == 0.0)
        assertTrue(b.distanceToPoint(Vector3(0.0, 0.0, 0.0)) == 0.0)
        assertTrue(b.distanceToPoint(Vector3(-1.0, -1.0, -1.0)) == 0.0)
        assertTrue(b.distanceToPoint(Vector3(-2.0, -2.0, -2.0)) == sqrt(3.0))
    }

    @Test
    fun getBoundingSphere() {
        val a = Box3(zero3, zero3)
        val b = Box3(zero3, one3)
        val c = Box3(one3.clone().negate(), one3)
        val sphere = Sphere()

        assertTrue(a.getBoundingSphere(sphere) == Sphere(zero3, 0.0))
        assertTrue(b.getBoundingSphere(sphere) == Sphere(one3.clone().multiplyScalar(0.5), sqrt(3.0) * 0.5))
        assertTrue(c.getBoundingSphere(sphere) == Sphere(zero3, sqrt(12.0) * 0.5))

        val d = Box3().makeEmpty()
        assertTrue(d.getBoundingSphere(sphere).isEmpty(), "Empty box's bounding sphere is empty")
    }

    @Test
    fun intersect() {
        val a = Box3(zero3, zero3)
        val b = Box3(zero3, one3)
        val c = Box3(one3.clone().negate(), one3)

        assertTrue(a.clone().intersect(a) == a)
        assertTrue(a.clone().intersect(b) == a)
        assertTrue(b.clone().intersect(b) == b)
        assertTrue(a.clone().intersect(c) == a)
        assertTrue(b.clone().intersect(c) == b)
        assertTrue(c.clone().intersect(c) == c)
    }

    @Test
    fun union() {
        val a = Box3(zero3, zero3)
        val b = Box3(zero3, one3)
        val c = Box3(one3.clone().negate(), one3)

        assertTrue(a.clone().union(a) == a)
        assertTrue(a.clone().union(b) == b)
        assertTrue(a.clone().union(c) == c)
        assertTrue(b.clone().union(c) == c)
    }

    @Test
    fun applyMatrix4() {
        val a = Box3(zero3, zero3)
        val b = Box3(zero3, one3)
        val c = Box3(one3.clone().negate(), one3)
        val d = Box3(one3.clone().negate(), zero3)

        val m = Matrix4().makeTranslation(1.0, -2.0, 1.0)
        val t1 = Vector3(1.0, -2.0, 1.0)

        assertTrue(compareBox(a.clone().applyMatrix4(m), a.clone().translate(t1)))
        assertTrue(compareBox(b.clone().applyMatrix4(m), b.clone().translate(t1)))
        assertTrue(compareBox(c.clone().applyMatrix4(m), c.clone().translate(t1)))
        assertTrue(compareBox(d.clone().applyMatrix4(m), d.clone().translate(t1)))
    }

    @Test
    fun translate() {
        val a = Box3(zero3, zero3)
        val b = Box3(zero3, one3)
        val c = Box3(one3.clone().negate(), zero3)

        assertTrue(a.clone().translate(one3) == Box3(one3, one3))
        assertTrue(a.clone().translate(one3).translate(one3.clone().negate()) == a)
        assertTrue(c.clone().translate(one3) == b)
        assertTrue(b.clone().translate(one3.clone().negate()) == c)
    }

    @Test
    fun equalsTest() {
        var a = Box3()
        var b = Box3()
        assertTrue(b == a)
        assertTrue(a == b)

        a = Box3(one3, two3)
        b = Box3(one3, two3)
        assertTrue(b == a)
        assertTrue(a == b)

        a = Box3(one3, two3)
        b = a.clone()
        assertTrue(b == a)
        assertTrue(a == b)

        a = Box3(one3, two3)
        b = Box3(one3, one3)
        assertFalse(b == a)
        assertFalse(a == b)

        a = Box3()
        b = Box3(one3, one3)
        assertFalse(b == a)
        assertFalse(a == b)
    }
}
