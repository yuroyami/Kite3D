/*
 * Copyright © 2026 yuroyami — MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Ray.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RayTest {

    @Test
    fun instancing() {
        var a = Ray()
        assertTrue(a.origin == zero3)
        assertTrue(a.direction == Vector3(0.0, 0.0, -1.0))

        a = Ray(two3, one3)
        assertTrue(a.origin == two3)
        assertTrue(a.direction == one3)
    }

    @Test
    fun set() {
        val a = Ray()

        a.set(one3, one3)
        assertTrue(a.origin == one3)
        assertTrue(a.direction == one3)
    }

    @Test
    fun recastClone() {
        val a = Ray(one3, Vector3(0.0, 0.0, 1.0))

        assertTrue(a.recast(0.0) == a)

        val b = a.clone()
        assertTrue(b.recast(-1.0) == Ray(Vector3(1.0, 1.0, 0.0), Vector3(0.0, 0.0, 1.0)))

        val c = a.clone()
        assertTrue(c.recast(1.0) == Ray(Vector3(1.0, 1.0, 2.0), Vector3(0.0, 0.0, 1.0)))

        val d = a.clone()
        val e = d.clone().recast(1.0)
        assertTrue(d == a)
        assertFalse(e == d)
        assertTrue(e == c)
    }

    @Test
    fun copyEquals() {
        val a = Ray(zero3, one3)
        val b = Ray().copy(a)
        assertTrue(b.origin == zero3)
        assertTrue(b.direction == one3)

        // ensure it is a true copy: mutating a's components must not affect b.
        // (three.js reassigns a.origin/a.direction; here they are `val`, so mutate.)
        a.origin.set(2.0, 2.0, 2.0)
        a.direction.set(3.0, 3.0, 3.0)
        assertTrue(b.origin == zero3)
        assertTrue(b.direction == one3)
    }

    @Test
    fun at() {
        val a = Ray(one3, Vector3(0.0, 0.0, 1.0))
        val point = Vector3()

        a.at(0.0, point)
        assertTrue(point == one3)
        a.at(-1.0, point)
        assertTrue(point == Vector3(1.0, 1.0, 0.0))
        a.at(1.0, point)
        assertTrue(point == Vector3(1.0, 1.0, 2.0))
    }

    @Test
    fun lookAt() {
        val a = Ray(two3, one3)
        val target = one3.clone()
        val expected = target.sub(two3).normalize()

        a.lookAt(target)
        assertTrue(a.direction == expected, "Check if we're looking in the right direction")
    }

    @Test
    fun closestPointToPoint() {
        val a = Ray(one3, Vector3(0.0, 0.0, 1.0))
        val point = Vector3()

        // behind the ray
        a.closestPointToPoint(zero3, point)
        assertTrue(point == one3)

        // front of the ray
        a.closestPointToPoint(Vector3(0.0, 0.0, 50.0), point)
        assertTrue(point == Vector3(1.0, 1.0, 50.0))

        // exactly on the ray
        a.closestPointToPoint(one3, point)
        assertTrue(point == one3)
    }

    @Test
    fun distanceToPoint() {
        val a = Ray(one3, Vector3(0.0, 0.0, 1.0))

        // behind the ray
        val b = a.distanceToPoint(zero3)
        assertTrue(b == sqrt(3.0))

        // front of the ray
        val c = a.distanceToPoint(Vector3(0.0, 0.0, 50.0))
        assertTrue(c == sqrt(2.0))

        // exactly on the ray
        val d = a.distanceToPoint(one3)
        assertTrue(d == 0.0)
    }

    @Test
    fun distanceSqToPoint() {
        val a = Ray(one3, Vector3(0.0, 0.0, 1.0))

        // behind the ray
        val b = a.distanceSqToPoint(zero3)
        assertTrue(b == 3.0)

        // front of the ray
        val c = a.distanceSqToPoint(Vector3(0.0, 0.0, 50.0))
        assertTrue(c == 2.0)

        // exactly on the ray
        val d = a.distanceSqToPoint(one3)
        assertTrue(d == 0.0)
    }

    @Test
    fun distanceSqToSegment() {
        val a = Ray(one3, Vector3(0.0, 0.0, 1.0))
        val ptOnLine = Vector3()
        val ptOnSegment = Vector3()

        // segment in front of the ray
        var v0 = Vector3(3.0, 5.0, 50.0)
        var v1 = Vector3(50.0, 50.0, 50.0) // just a far away point
        var distSqr = a.distanceSqToSegment(v0, v1, ptOnLine, ptOnSegment)

        assertTrue(ptOnSegment.distanceTo(v0) < 0.0001)
        assertTrue(ptOnLine.distanceTo(Vector3(1.0, 1.0, 50.0)) < 0.0001)
        // ((3-1) * (3-1) + (5-1) * (5-1) = 4 + 16 = 20
        assertTrue(abs(distSqr - 20) < 0.0001)

        // segment behind the ray
        v0 = Vector3(-50.0, -50.0, -50.0) // just a far away point
        v1 = Vector3(-3.0, -5.0, -4.0)
        distSqr = a.distanceSqToSegment(v0, v1, ptOnLine, ptOnSegment)

        assertTrue(ptOnSegment.distanceTo(v1) < 0.0001)
        assertTrue(ptOnLine.distanceTo(one3) < 0.0001)
        // ((-3-1) * (-3-1) + (-5-1) * (-5-1) + (-4-1) + (-4-1) = 16 + 36 + 25 = 77
        assertTrue(abs(distSqr - 77) < 0.0001)

        // exact intersection between the ray and the segment
        v0 = Vector3(-50.0, -50.0, -50.0)
        v1 = Vector3(50.0, 50.0, 50.0)
        distSqr = a.distanceSqToSegment(v0, v1, ptOnLine, ptOnSegment)

        assertTrue(ptOnSegment.distanceTo(one3) < 0.0001)
        assertTrue(ptOnLine.distanceTo(one3) < 0.0001)
        assertTrue(distSqr < 0.0001)
    }

    @Test
    fun intersectSphere() {
        val tol = 0.0001
        val point = Vector3()

        // ray a0 origin located at ( 0, 0, 0 ) and points outward in negative-z direction
        val a0 = Ray(zero3, Vector3(0.0, 0.0, -1.0))
        // ray a1 origin located at ( 1, 1, 1 ) and points left in negative-x direction
        val a1 = Ray(one3, Vector3(-1.0, 0.0, 0.0))

        // sphere (radius of 2) located behind ray a0, should result in null
        var b = Sphere(Vector3(0.0, 0.0, 3.0), 2.0)
        a0.intersectSphere(b, point.copy(posInf3))
        assertTrue(point == posInf3)

        // sphere (radius of 2) located in front of, but too far right of ray a0, should result in null
        b = Sphere(Vector3(3.0, 0.0, -1.0), 2.0)
        a0.intersectSphere(b, point.copy(posInf3))
        assertTrue(point == posInf3)

        // sphere (radius of 2) located below ray a1, should result in null
        b = Sphere(Vector3(1.0, -2.0, 1.0), 2.0)
        a1.intersectSphere(b, point.copy(posInf3))
        assertTrue(point == posInf3)

        // sphere (radius of 1) located to the left of ray a1, should result in intersection at 0, 1, 1
        b = Sphere(Vector3(-1.0, 1.0, 1.0), 1.0)
        a1.intersectSphere(b, point)
        assertTrue(point.distanceTo(Vector3(0.0, 1.0, 1.0)) < tol)

        // sphere (radius of 1) located in front of ray a0, should result in intersection at 0, 0, -1
        b = Sphere(Vector3(0.0, 0.0, -2.0), 1.0)
        a0.intersectSphere(b, point)
        assertTrue(point.distanceTo(Vector3(0.0, 0.0, -1.0)) < tol)

        // sphere (radius of 2) located in front & right of ray a0, should result in intersection at 0, 0, -1, or left-most edge of sphere
        b = Sphere(Vector3(2.0, 0.0, -1.0), 2.0)
        a0.intersectSphere(b, point)
        assertTrue(point.distanceTo(Vector3(0.0, 0.0, -1.0)) < tol)

        // same situation as above, but move the sphere a fraction more to the right, and ray a0 should now just miss
        b = Sphere(Vector3(2.01, 0.0, -1.0), 2.0)
        a0.intersectSphere(b, point.copy(posInf3))
        assertTrue(point == posInf3)

        // following tests are for situations where the ray origin is inside the sphere

        // sphere (radius of 1) center located at ray a0 origin / sphere surrounds the ray origin, so the first intersect point 0, 0, 1,
        // is behind ray a0.  Therefore, second exit point on back of sphere will be returned: 0, 0, -1
        // thus keeping the intersection point always in front of the ray.
        b = Sphere(zero3, 1.0)
        a0.intersectSphere(b, point)
        assertTrue(point.distanceTo(Vector3(0.0, 0.0, -1.0)) < tol)

        // sphere (radius of 4) center located behind ray a0 origin / sphere surrounds the ray origin, so the first intersect point 0, 0, 5,
        // is behind ray a0.  Therefore, second exit point on back of sphere will be returned: 0, 0, -3
        // thus keeping the intersection point always in front of the ray.
        b = Sphere(Vector3(0.0, 0.0, 1.0), 4.0)
        a0.intersectSphere(b, point)
        assertTrue(point.distanceTo(Vector3(0.0, 0.0, -3.0)) < tol)

        // sphere (radius of 4) center located in front of ray a0 origin / sphere surrounds the ray origin, so the first intersect point 0, 0, 3,
        // is behind ray a0.  Therefore, second exit point on back of sphere will be returned: 0, 0, -5
        // thus keeping the intersection point always in front of the ray.
        b = Sphere(Vector3(0.0, 0.0, -1.0), 4.0)
        a0.intersectSphere(b, point)
        assertTrue(point.distanceTo(Vector3(0.0, 0.0, -5.0)) < tol)
    }

    @Test
    fun intersectsSphere() {
        val a = Ray(one3, Vector3(0.0, 0.0, 1.0))
        val b = Sphere(zero3, 0.5)
        val c = Sphere(zero3, 1.5)
        val d = Sphere(one3, 0.1)
        val e = Sphere(two3, 0.1)
        val f = Sphere(two3, 1.0)

        assertFalse(a.intersectsSphere(b))
        assertFalse(a.intersectsSphere(c))
        assertTrue(a.intersectsSphere(d))
        assertFalse(a.intersectsSphere(e))
        assertFalse(a.intersectsSphere(f))
    }

    @Test
    fun intersectPlane() {
        val a = Ray(one3, Vector3(0.0, 0.0, 1.0))
        val point = Vector3()

        // parallel plane behind
        val b = Plane().setFromNormalAndCoplanarPoint(Vector3(0.0, 0.0, 1.0), Vector3(1.0, 1.0, -1.0))
        a.intersectPlane(b, point.copy(posInf3))
        assertTrue(point == posInf3)

        // parallel plane coincident with origin
        val c = Plane().setFromNormalAndCoplanarPoint(Vector3(0.0, 0.0, 1.0), Vector3(1.0, 1.0, 0.0))
        a.intersectPlane(c, point.copy(posInf3))
        assertTrue(point == posInf3)

        // parallel plane in front
        val d = Plane().setFromNormalAndCoplanarPoint(Vector3(0.0, 0.0, 1.0), Vector3(1.0, 1.0, 1.0))
        a.intersectPlane(d, point.copy(posInf3))
        assertTrue(point == a.origin)

        // perpendicular ray that overlaps exactly
        val e = Plane().setFromNormalAndCoplanarPoint(Vector3(1.0, 0.0, 0.0), one3)
        a.intersectPlane(e, point.copy(posInf3))
        assertTrue(point == a.origin)

        // perpendicular ray that doesn't overlap
        val f = Plane().setFromNormalAndCoplanarPoint(Vector3(1.0, 0.0, 0.0), zero3)
        a.intersectPlane(f, point.copy(posInf3))
        assertTrue(point == posInf3)
    }

    @Test
    fun intersectsPlane() {
        val a = Ray(one3, Vector3(0.0, 0.0, 1.0))

        // parallel plane in front of the ray
        val b = Plane().setFromNormalAndCoplanarPoint(Vector3(0.0, 0.0, 1.0), one3.clone().sub(Vector3(0.0, 0.0, -1.0)))
        assertTrue(a.intersectsPlane(b))

        // parallel plane coincident with origin
        val c = Plane().setFromNormalAndCoplanarPoint(Vector3(0.0, 0.0, 1.0), one3.clone().sub(Vector3(0.0, 0.0, 0.0)))
        assertTrue(a.intersectsPlane(c))

        // parallel plane behind the ray
        val d = Plane().setFromNormalAndCoplanarPoint(Vector3(0.0, 0.0, 1.0), one3.clone().sub(Vector3(0.0, 0.0, 1.0)))
        assertFalse(a.intersectsPlane(d))

        // perpendicular ray that overlaps exactly
        val e = Plane().setFromNormalAndCoplanarPoint(Vector3(1.0, 0.0, 0.0), one3)
        assertTrue(a.intersectsPlane(e))

        // perpendicular ray that doesn't overlap
        val f = Plane().setFromNormalAndCoplanarPoint(Vector3(1.0, 0.0, 0.0), zero3)
        assertFalse(a.intersectsPlane(f))
    }

    @Test
    fun intersectBox() {
        val tol = 0.0001

        val box = Box3(Vector3(-1.0, -1.0, -1.0), Vector3(1.0, 1.0, 1.0))
        val point = Vector3()

        val a = Ray(Vector3(-2.0, 0.0, 0.0), Vector3(1.0, 0.0, 0.0))
        // ray should intersect box at -1,0,0
        assertTrue(a.intersectsBox(box))
        a.intersectBox(box, point)
        assertTrue(point.distanceTo(Vector3(-1.0, 0.0, 0.0)) < tol)

        val b = Ray(Vector3(-2.0, 0.0, 0.0), Vector3(-1.0, 0.0, 0.0))
        // ray is point away from box, it should not intersect
        assertFalse(b.intersectsBox(box))
        b.intersectBox(box, point.copy(posInf3))
        assertTrue(point == posInf3)

        val c = Ray(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 0.0, 0.0))
        // ray is inside box, should return exit point
        assertTrue(c.intersectsBox(box))
        c.intersectBox(box, point)
        assertTrue(point.distanceTo(Vector3(1.0, 0.0, 0.0)) < tol)

        val d = Ray(Vector3(0.0, 2.0, 1.0), Vector3(0.0, -1.0, -1.0).normalize())
        // tilted ray should intersect box at 0,1,0
        assertTrue(d.intersectsBox(box))
        d.intersectBox(box, point)
        assertTrue(point.distanceTo(Vector3(0.0, 1.0, 0.0)) < tol)

        val e = Ray(Vector3(1.0, -2.0, 1.0), Vector3(0.0, 1.0, 0.0).normalize())
        // handle case where ray is coplanar with one of the boxes side - box in front of ray
        assertTrue(e.intersectsBox(box))
        e.intersectBox(box, point)
        assertTrue(point.distanceTo(Vector3(1.0, -1.0, 1.0)) < tol)

        val f = Ray(Vector3(1.0, -2.0, 0.0), Vector3(0.0, -1.0, 0.0).normalize())
        // handle case where ray is coplanar with one of the boxes side - box behind ray
        assertFalse(f.intersectsBox(box))
        f.intersectBox(box, point.copy(posInf3))
        assertTrue(point == posInf3)
    }

    @Test
    fun intersectTriangle() {
        val ray = Ray()
        val a = Vector3(1.0, 1.0, 0.0)
        val b = Vector3(0.0, 1.0, 1.0)
        val c = Vector3(1.0, 0.0, 1.0)
        val point = Vector3()

        // DdN == 0
        ray.set(ray.origin, zero3.clone())
        ray.intersectTriangle(a, b, c, false, point.copy(posInf3))
        assertTrue(point == posInf3, "No intersection if direction == zero")

        // DdN > 0, backfaceCulling = true
        ray.set(ray.origin, one3.clone())
        ray.intersectTriangle(a, b, c, true, point.copy(posInf3))
        assertTrue(point == posInf3, "No intersection with backside faces if backfaceCulling is true")

        // DdN > 0
        ray.set(ray.origin, one3.clone())
        ray.intersectTriangle(a, b, c, false, point)
        assertTrue(abs(point.x - 2.0 / 3.0) <= eps, "Successful intersection: check x")
        assertTrue(abs(point.y - 2.0 / 3.0) <= eps, "Successful intersection: check y")
        assertTrue(abs(point.z - 2.0 / 3.0) <= eps, "Successful intersection: check z")

        // DdN > 0, DdQxE2 < 0
        b.multiplyScalar(-1.0)
        ray.intersectTriangle(a, b, c, false, point.copy(posInf3))
        assertTrue(point == posInf3, "No intersection")

        // DdN > 0, DdE1xQ < 0
        a.multiplyScalar(-1.0)
        ray.intersectTriangle(a, b, c, false, point.copy(posInf3))
        assertTrue(point == posInf3, "No intersection")

        // DdN > 0, DdQxE2 + DdE1xQ > DdN
        b.multiplyScalar(-1.0)
        ray.intersectTriangle(a, b, c, false, point.copy(posInf3))
        assertTrue(point == posInf3, "No intersection")

        // DdN < 0, QdN < 0
        a.multiplyScalar(-1.0)
        b.multiplyScalar(-1.0)
        ray.direction.multiplyScalar(-1.0)
        ray.intersectTriangle(a, b, c, false, point.copy(posInf3))
        assertTrue(point == posInf3, "No intersection when looking in the wrong direction")
    }

    @Test
    fun applyMatrix4() {
        var a = Ray(one3, Vector3(0.0, 0.0, 1.0))
        val m = Matrix4()

        assertTrue(a.clone().applyMatrix4(m) == a)

        a = Ray(zero3, Vector3(0.0, 0.0, 1.0))
        m.makeRotationZ(PI)
        assertTrue(a.clone().applyMatrix4(m) == a)

        m.makeRotationX(PI)
        val b = a.clone()
        b.direction.negate()
        var a2 = a.clone().applyMatrix4(m)
        assertTrue(a2.origin.distanceTo(b.origin) < 0.0001)
        assertTrue(a2.direction.distanceTo(b.direction) < 0.0001)

        // three.js reassigns a.origin/b.origin here; origin is `val`, so mutate it.
        a.origin.set(0.0, 0.0, 1.0)
        b.origin.set(0.0, 0.0, -1.0)
        a2 = a.clone().applyMatrix4(m)
        assertTrue(a2.origin.distanceTo(b.origin) < 0.0001)
        assertTrue(a2.direction.distanceTo(b.direction) < 0.0001)
    }
}
