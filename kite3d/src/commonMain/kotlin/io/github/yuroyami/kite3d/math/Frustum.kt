/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Frustum.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

/**
 * Frustums are used to determine what is inside the camera's field of view.
 * They help speed up the rendering process. Objects which lie outside a camera's
 * frustum can safely be excluded from rendering.
 *
 * This class is mainly intended for use internally by a renderer.
 *
 * Frustums are **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js.
 *
 * The constructor stores the given [Plane] instances by reference.
 *
 * @param p0 The first plane that encloses the frustum.
 * @param p1 The second plane that encloses the frustum.
 * @param p2 The third plane that encloses the frustum.
 * @param p3 The fourth plane that encloses the frustum.
 * @param p4 The fifth plane that encloses the frustum.
 * @param p5 The sixth plane that encloses the frustum.
 */
public class Frustum(
    p0: Plane = Plane(),
    p1: Plane = Plane(),
    p2: Plane = Plane(),
    p3: Plane = Plane(),
    p4: Plane = Plane(),
    p5: Plane = Plane(),
) {

    /**
     * This array holds the planes that enclose the frustum.
     *
     * Exposed as a `val`: three.js never reassigns the array, only mutates its
     * contents.
     */
    public val planes: Array<Plane> = arrayOf(p0, p1, p2, p3, p4, p5)

    /**
     * Sets the frustum planes by copying the given planes.
     *
     * @param p0 The first plane that encloses the frustum.
     * @param p1 The second plane that encloses the frustum.
     * @param p2 The third plane that encloses the frustum.
     * @param p3 The fourth plane that encloses the frustum.
     * @param p4 The fifth plane that encloses the frustum.
     * @param p5 The sixth plane that encloses the frustum.
     * @return A reference to this frustum.
     */
    public fun set(p0: Plane, p1: Plane, p2: Plane, p3: Plane, p4: Plane, p5: Plane): Frustum {
        val planes = this.planes

        planes[0].copy(p0)
        planes[1].copy(p1)
        planes[2].copy(p2)
        planes[3].copy(p3)
        planes[4].copy(p4)
        planes[5].copy(p5)

        return this
    }

    /**
     * Copies the values of the given frustum to this instance.
     *
     * @param frustum The frustum to copy.
     * @return A reference to this frustum.
     */
    public fun copy(frustum: Frustum): Frustum {
        val planes = this.planes

        for (i in 0 until 6) {
            planes[i].copy(frustum.planes[i])
        }

        return this
    }

    /**
     * Sets the frustum planes from the given projection matrix.
     *
     * @param m The projection matrix.
     * @param coordinateSystem The coordinate system.
     * @param reversedDepth Whether to use a reversed depth.
     * @return A reference to this frustum.
     */
    public fun setFromProjectionMatrix(
        m: Matrix4,
        coordinateSystem: CoordinateSystem = CoordinateSystem.WebGL,
        reversedDepth: Boolean = false,
    ): Frustum {
        val planes = this.planes
        val me = m.elements
        val me0 = me[0]; val me1 = me[1]; val me2 = me[2]; val me3 = me[3]
        val me4 = me[4]; val me5 = me[5]; val me6 = me[6]; val me7 = me[7]
        val me8 = me[8]; val me9 = me[9]; val me10 = me[10]; val me11 = me[11]
        val me12 = me[12]; val me13 = me[13]; val me14 = me[14]; val me15 = me[15]

        planes[0].setComponents(me3 - me0, me7 - me4, me11 - me8, me15 - me12).normalize()
        planes[1].setComponents(me3 + me0, me7 + me4, me11 + me8, me15 + me12).normalize()
        planes[2].setComponents(me3 + me1, me7 + me5, me11 + me9, me15 + me13).normalize()
        planes[3].setComponents(me3 - me1, me7 - me5, me11 - me9, me15 - me13).normalize()

        if (reversedDepth) {
            planes[4].setComponents(me2, me6, me10, me14).normalize() // far
            planes[5].setComponents(me3 - me2, me7 - me6, me11 - me10, me15 - me14).normalize() // near
        } else {
            planes[4].setComponents(me3 - me2, me7 - me6, me11 - me10, me15 - me14).normalize() // far

            when (coordinateSystem) {
                CoordinateSystem.WebGL ->
                    planes[5].setComponents(me3 + me2, me7 + me6, me11 + me10, me15 + me14).normalize() // near
                CoordinateSystem.WebGPU ->
                    planes[5].setComponents(me2, me6, me10, me14).normalize() // near
            }
        }

        return this
    }

    // intersectsObject(object) and intersectsSprite(sprite) are DEFERRED: they need
    // the core Object3D / Sprite / BufferGeometry types (boundingSphere,
    // computeBoundingSphere, matrixWorld) which are not part of the math layer.
    // See port-ledger.yaml. They will be reintroduced (likely as ext functions in
    // the owning layer) once core lands. Their upstream tests are skipped in
    // FrustumTest.kt for the same reason.

    /**
     * Returns `true` if the given bounding sphere is intersecting this frustum.
     *
     * @param sphere The bounding sphere to test.
     * @return Whether the bounding sphere is intersecting this frustum or not.
     */
    public fun intersectsSphere(sphere: Sphere): Boolean {
        val planes = this.planes
        val center = sphere.center
        val negRadius = -sphere.radius

        for (i in 0 until 6) {
            val distance = planes[i].distanceToPoint(center)

            if (distance < negRadius) {
                return false
            }
        }

        return true
    }

    /**
     * Returns `true` if the given bounding box is intersecting this frustum.
     *
     * @param box The bounding box to test.
     * @return Whether the bounding box is intersecting this frustum or not.
     */
    public fun intersectsBox(box: Box3): Boolean {
        val planes = this.planes
        // _vector module temp inlined as a local (dialect rule 13).
        val vector = Vector3()

        for (i in 0 until 6) {
            val plane = planes[i]

            // corner at max distance
            vector.x = if (plane.normal.x > 0) box.max.x else box.min.x
            vector.y = if (plane.normal.y > 0) box.max.y else box.min.y
            vector.z = if (plane.normal.z > 0) box.max.z else box.min.z

            if (plane.distanceToPoint(vector) < 0) {
                return false
            }
        }

        return true
    }

    /**
     * Returns `true` if the given point lies within the frustum.
     *
     * @param point The point to test.
     * @return Whether the point lies within this frustum or not.
     */
    public fun containsPoint(point: Vector3): Boolean {
        val planes = this.planes

        for (i in 0 until 6) {
            if (planes[i].distanceToPoint(point) < 0) {
                return false
            }
        }

        return true
    }

    /**
     * Returns a new frustum with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Frustum = Frustum().copy(this)
}
