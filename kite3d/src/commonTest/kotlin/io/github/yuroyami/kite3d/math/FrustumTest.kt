/*
 * Copyright © 2026 yuroyami — MIT.
 * Ported to Kotlin for Kite3D from three.js r184 test/unit/src/math/Frustum.tests.js (MIT).
 */
package io.github.yuroyami.kite3d.math

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FrustumTest {

    // Local copy of the JS test's `unit3 = new Vector3(1, 0, 0)`. Fresh per use to
    // avoid cross-test aliasing (Plane stores its normal by reference).
    private val unit3 get() = Vector3(1.0, 0.0, 0.0)

    @Test
    fun instancing() {
        var a = Frustum()

        assertTrue(a.planes.size == 6)

        val pDefault = Plane()
        for (i in 0 until 6) {
            assertTrue(a.planes[i] == pDefault)
        }

        val p0 = Plane(unit3, -1.0)
        val p1 = Plane(unit3, 1.0)
        val p2 = Plane(unit3, 2.0)
        val p3 = Plane(unit3, 3.0)
        val p4 = Plane(unit3, 4.0)
        val p5 = Plane(unit3, 5.0)

        a = Frustum(p0, p1, p2, p3, p4, p5)
        assertTrue(a.planes[0] == p0)
        assertTrue(a.planes[1] == p1)
        assertTrue(a.planes[2] == p2)
        assertTrue(a.planes[3] == p3)
        assertTrue(a.planes[4] == p4)
        assertTrue(a.planes[5] == p5)
    }

    @Test
    fun set() {
        val a = Frustum()
        val p0 = Plane(unit3, -1.0)
        val p1 = Plane(unit3, 1.0)
        val p2 = Plane(unit3, 2.0)
        val p3 = Plane(unit3, 3.0)
        val p4 = Plane(unit3, 4.0)
        val p5 = Plane(unit3, 5.0)

        a.set(p0, p1, p2, p3, p4, p5)

        assertTrue(a.planes[0] == p0)
        assertTrue(a.planes[1] == p1)
        assertTrue(a.planes[2] == p2)
        assertTrue(a.planes[3] == p3)
        assertTrue(a.planes[4] == p4)
        assertTrue(a.planes[5] == p5)
    }

    @Test
    fun clone() {
        val p0 = Plane(unit3, -1.0)
        val p1 = Plane(unit3, 1.0)
        val p2 = Plane(unit3, 2.0)
        val p3 = Plane(unit3, 3.0)
        val p4 = Plane(unit3, 4.0)
        val p5 = Plane(unit3, 5.0)

        val b = Frustum(p0, p1, p2, p3, p4, p5)
        val a = b.clone()
        assertTrue(a.planes[0] == p0)
        assertTrue(a.planes[1] == p1)
        assertTrue(a.planes[2] == p2)
        assertTrue(a.planes[3] == p3)
        assertTrue(a.planes[4] == p4)
        assertTrue(a.planes[5] == p5)

        // ensure it is a true copy by modifying source
        a.planes[0].copy(p1)
        assertTrue(b.planes[0] == p0)
    }

    @Test
    fun copy() {
        val p0 = Plane(unit3, -1.0)
        val p1 = Plane(unit3, 1.0)
        val p2 = Plane(unit3, 2.0)
        val p3 = Plane(unit3, 3.0)
        val p4 = Plane(unit3, 4.0)
        val p5 = Plane(unit3, 5.0)

        val b = Frustum(p0, p1, p2, p3, p4, p5)
        val a = Frustum().copy(b)
        assertTrue(a.planes[0] == p0)
        assertTrue(a.planes[1] == p1)
        assertTrue(a.planes[2] == p2)
        assertTrue(a.planes[3] == p3)
        assertTrue(a.planes[4] == p4)
        assertTrue(a.planes[5] == p5)

        // ensure it is a true copy by modifying source
        b.planes[0] = p1
        assertTrue(a.planes[0] == p0)
    }

    @Test
    fun setFromProjectionMatrix_makeOrthographic_containsPoint() {
        val m = Matrix4().makeOrthographic(-1.0, 1.0, 1.0, -1.0, 1.0, 100.0)
        val a = Frustum().setFromProjectionMatrix(m)

        assertFalse(a.containsPoint(Vector3(0.0, 0.0, 0.0)))
        assertTrue(a.containsPoint(Vector3(0.0, 0.0, -50.0)))
        assertTrue(a.containsPoint(Vector3(0.0, 0.0, -1.001)))
        assertTrue(a.containsPoint(Vector3(-1.0, -1.0, -1.001)))
        assertFalse(a.containsPoint(Vector3(-1.1, -1.1, -1.001)))
        assertTrue(a.containsPoint(Vector3(1.0, 1.0, -1.001)))
        assertFalse(a.containsPoint(Vector3(1.1, 1.1, -1.001)))
        assertTrue(a.containsPoint(Vector3(0.0, 0.0, -99.999)))
        assertTrue(a.containsPoint(Vector3(-0.999, -0.999, -99.999)))
        assertFalse(a.containsPoint(Vector3(-1.1, -1.1, -100.1)))
        assertTrue(a.containsPoint(Vector3(0.999, 0.999, -99.999)))
        assertFalse(a.containsPoint(Vector3(1.1, 1.1, -100.1)))
        assertFalse(a.containsPoint(Vector3(0.0, 0.0, -101.0)))
    }

    @Test
    fun setFromProjectionMatrix_makePerspective_containsPoint() {
        val m = Matrix4().makePerspective(-1.0, 1.0, 1.0, -1.0, 1.0, 100.0)
        val a = Frustum().setFromProjectionMatrix(m)

        assertFalse(a.containsPoint(Vector3(0.0, 0.0, 0.0)))
        assertTrue(a.containsPoint(Vector3(0.0, 0.0, -50.0)))
        assertTrue(a.containsPoint(Vector3(0.0, 0.0, -1.001)))
        assertTrue(a.containsPoint(Vector3(-1.0, -1.0, -1.001)))
        assertFalse(a.containsPoint(Vector3(-1.1, -1.1, -1.001)))
        assertTrue(a.containsPoint(Vector3(1.0, 1.0, -1.001)))
        assertFalse(a.containsPoint(Vector3(1.1, 1.1, -1.001)))
        assertTrue(a.containsPoint(Vector3(0.0, 0.0, -99.999)))
        assertTrue(a.containsPoint(Vector3(-99.999, -99.999, -99.999)))
        assertFalse(a.containsPoint(Vector3(-100.1, -100.1, -100.1)))
        assertTrue(a.containsPoint(Vector3(99.999, 99.999, -99.999)))
        assertFalse(a.containsPoint(Vector3(100.1, 100.1, -100.1)))
        assertFalse(a.containsPoint(Vector3(0.0, 0.0, -101.0)))
    }

    @Test
    fun setFromProjectionMatrix_makePerspective_intersectsSphere() {
        val m = Matrix4().makePerspective(-1.0, 1.0, 1.0, -1.0, 1.0, 100.0)
        val a = Frustum().setFromProjectionMatrix(m)

        assertFalse(a.intersectsSphere(Sphere(Vector3(0.0, 0.0, 0.0), 0.0)))
        assertFalse(a.intersectsSphere(Sphere(Vector3(0.0, 0.0, 0.0), 0.9)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(0.0, 0.0, 0.0), 1.1)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(0.0, 0.0, -50.0), 0.0)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(0.0, 0.0, -1.001), 0.0)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(-1.0, -1.0, -1.001), 0.0)))
        assertFalse(a.intersectsSphere(Sphere(Vector3(-1.1, -1.1, -1.001), 0.0)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(-1.1, -1.1, -1.001), 0.5)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(1.0, 1.0, -1.001), 0.0)))
        assertFalse(a.intersectsSphere(Sphere(Vector3(1.1, 1.1, -1.001), 0.0)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(1.1, 1.1, -1.001), 0.5)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(0.0, 0.0, -99.999), 0.0)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(-99.999, -99.999, -99.999), 0.0)))
        assertFalse(a.intersectsSphere(Sphere(Vector3(-100.1, -100.1, -100.1), 0.0)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(-100.1, -100.1, -100.1), 0.5)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(99.999, 99.999, -99.999), 0.0)))
        assertFalse(a.intersectsSphere(Sphere(Vector3(100.1, 100.1, -100.1), 0.0)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(100.1, 100.1, -100.1), 0.2)))
        assertFalse(a.intersectsSphere(Sphere(Vector3(0.0, 0.0, -101.0), 0.0)))
        assertTrue(a.intersectsSphere(Sphere(Vector3(0.0, 0.0, -101.0), 1.1)))
    }

    // intersectsObject and intersectsSprite tests are SKIPPED: Frustum.intersectsObject
    // / intersectsSprite are DEFERRED (need core Mesh / Sprite / BoxGeometry /
    // Object3D). See Frustum.kt and port-ledger.yaml.

    @Test
    fun intersectsBox() {
        val m = Matrix4().makePerspective(-1.0, 1.0, 1.0, -1.0, 1.0, 100.0)
        val a = Frustum().setFromProjectionMatrix(m)
        val box = Box3(zero3.clone(), one3.clone())

        assertFalse(a.intersectsBox(box))

        // add eps so that we prevent box touching the frustum,
        // which might intersect depending on floating point numerics
        box.translate(Vector3(-1.0 - eps, -1.0 - eps, -1.0 - eps))

        assertTrue(a.intersectsBox(box))
    }
}
