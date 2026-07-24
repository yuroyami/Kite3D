/*
 * Copyright (c) 2026 yuroyami - MIT.
 * Ported to Kotlin for Kite3D from three.js r184 src/math/Triangle.js (MIT).
 * Original three.js (c) 2010-2026 three.js authors.
 */
package io.github.yuroyami.kite3d.math

import kotlin.math.sqrt

/**
 * A geometric triangle as defined by three vectors representing its three corners.
 *
 * Triangles are **mutable** and **not thread-safe**; confine an instance (and any
 * object graph holding it) to a single thread, exactly as in three.js. Most methods
 * mutate `this` and return it for chaining.
 *
 * The constructor stores the given [Vector3] instances by reference, so
 * `Triangle(v, v, v)` shares `v` across all three corners.
 */
public class Triangle(
    /** The first corner of the triangle. */
    public var a: Vector3 = Vector3(),
    /** The second corner of the triangle. */
    public var b: Vector3 = Vector3(),
    /** The third corner of the triangle. */
    public var c: Vector3 = Vector3(),
) {

    public companion object {

        /**
         * Computes the normal vector of a triangle.
         *
         * @param a The first corner of the triangle.
         * @param b The second corner of the triangle.
         * @param c The third corner of the triangle.
         * @param target The target vector that is used to store the method's result.
         * @return The triangle's normal.
         */
        public fun getNormal(a: Vector3, b: Vector3, c: Vector3, target: Vector3): Vector3 {
            // _v0 module temp inlined as a local (dialect rule 13).
            val v0 = Vector3()

            target.subVectors(c, b)
            v0.subVectors(a, b)
            target.cross(v0)

            val targetLengthSq = target.lengthSq()
            if (targetLengthSq > 0) {
                return target.multiplyScalar(1.0 / sqrt(targetLengthSq))
            }

            return target.set(0.0, 0.0, 0.0)
        }

        /**
         * Computes a barycentric coordinates from the given vector.
         * Returns `null` if the triangle is degenerate.
         *
         * @param point A point in 3D space.
         * @param a The first corner of the triangle.
         * @param b The second corner of the triangle.
         * @param c The third corner of the triangle.
         * @param target The target vector that is used to store the method's result.
         * @return The barycentric coordinates for the given point, or `null` if degenerate.
         */
        public fun getBarycoord(point: Vector3, a: Vector3, b: Vector3, c: Vector3, target: Vector3): Vector3? {
            // based on: http://www.blackpawn.com/texts/pointinpoly/default.html
            val v0 = Vector3()
            val v1 = Vector3()
            val v2 = Vector3()

            v0.subVectors(c, a)
            v1.subVectors(b, a)
            v2.subVectors(point, a)

            val dot00 = v0.dot(v0)
            val dot01 = v0.dot(v1)
            val dot02 = v0.dot(v2)
            val dot11 = v1.dot(v1)
            val dot12 = v1.dot(v2)

            val denom = (dot00 * dot11 - dot01 * dot01)

            // collinear or singular triangle
            if (denom == 0.0) {
                target.set(0.0, 0.0, 0.0)
                return null
            }

            val invDenom = 1.0 / denom
            val u = (dot11 * dot02 - dot01 * dot12) * invDenom
            val v = (dot00 * dot12 - dot01 * dot02) * invDenom

            // barycentric coordinates must always sum to 1
            return target.set(1.0 - u - v, v, u)
        }

        /**
         * Returns `true` if the given point, when projected onto the plane of the
         * triangle, lies within the triangle.
         *
         * @param point The point in 3D space to test.
         * @param a The first corner of the triangle.
         * @param b The second corner of the triangle.
         * @param c The third corner of the triangle.
         * @return Whether the given point, when projected onto the plane of the
         * triangle, lies within the triangle or not.
         */
        public fun containsPoint(point: Vector3, a: Vector3, b: Vector3, c: Vector3): Boolean {
            val v3 = Vector3()

            // if the triangle is degenerate then we can't contain a point
            if (getBarycoord(point, a, b, c, v3) == null) {
                return false
            }

            return (v3.x >= 0) && (v3.y >= 0) && ((v3.x + v3.y) <= 1)
        }

        /**
         * Computes the value barycentrically interpolated for the given point on the
         * triangle. Returns `null` if the triangle is degenerate.
         *
         * @param point Position of interpolated point.
         * @param p1 The first corner of the triangle.
         * @param p2 The second corner of the triangle.
         * @param p3 The third corner of the triangle.
         * @param v1 Value to interpolate of first vertex.
         * @param v2 Value to interpolate of second vertex.
         * @param v3 Value to interpolate of third vertex.
         * @param target The target vector that is used to store the method's result.
         * @return The interpolated value, or `null` if the triangle is degenerate.
         */
        public fun getInterpolation(
            point: Vector3,
            p1: Vector3,
            p2: Vector3,
            p3: Vector3,
            v1: Vector3,
            v2: Vector3,
            v3: Vector3,
            target: Vector3,
        ): Vector3? {
            val bary = Vector3()

            if (getBarycoord(point, p1, p2, p3, bary) == null) {
                // Upstream also zeros target.x/y and (when present) .z/.w. The
                // Vector3-target variant ported here always has z (and no w).
                target.x = 0.0
                target.y = 0.0
                target.z = 0.0
                return null
            }

            target.setScalar(0.0)
            target.addScaledVector(v1, bary.x)
            target.addScaledVector(v2, bary.y)
            target.addScaledVector(v3, bary.z)

            return target
        }

        /**
         * Computes the value barycentrically interpolated for the given attribute
         * and indices.
         *
         * @param attr The attribute to interpolate.
         * @param i1 Index of first vertex.
         * @param i2 Index of second vertex.
         * @param i3 Index of third vertex.
         * @param barycoord The barycoordinate value to use to interpolate.
         * @param target The target vector that is used to store the method's result.
         * @return The interpolated attribute value.
         */
        public fun getInterpolatedAttribute(
            attr: AttributeLike,
            i1: Int,
            i2: Int,
            i3: Int,
            barycoord: Vector3,
            target: Vector3,
        ): Vector3 {
            // _v40/_v41/_v42 module temps inlined as locals. Upstream uses Vector4;
            // only getX/Y/Z/W are read through the AttributeLike seam.
            val v40 = Vector4()
            val v41 = Vector4()
            val v42 = Vector4()

            v40.setScalar(0.0)
            v41.setScalar(0.0)
            v42.setScalar(0.0)

            v40.fromBufferAttribute(attr, i1)
            v41.fromBufferAttribute(attr, i2)
            v42.fromBufferAttribute(attr, i3)

            target.setScalar(0.0)
            target.addScaledVector(Vector3(v40.x, v40.y, v40.z), barycoord.x)
            target.addScaledVector(Vector3(v41.x, v41.y, v41.z), barycoord.y)
            target.addScaledVector(Vector3(v42.x, v42.y, v42.z), barycoord.z)

            return target
        }

        /**
         * Returns `true` if the triangle is oriented towards the given direction.
         *
         * @param a The first corner of the triangle.
         * @param b The second corner of the triangle.
         * @param c The third corner of the triangle.
         * @param direction The (normalized) direction vector.
         * @return Whether the triangle is oriented towards the given direction or not.
         */
        public fun isFrontFacing(a: Vector3, b: Vector3, c: Vector3, direction: Vector3): Boolean {
            val v0 = Vector3()
            val v1 = Vector3()

            v0.subVectors(c, b)
            v1.subVectors(a, b)

            // strictly front facing
            return v0.cross(v1).dot(direction) < 0
        }
    }

    /**
     * Sets the triangle's vertices by copying the given values.
     *
     * @param a The first corner of the triangle.
     * @param b The second corner of the triangle.
     * @param c The third corner of the triangle.
     * @return A reference to this triangle.
     */
    public fun set(a: Vector3, b: Vector3, c: Vector3): Triangle {
        this.a.copy(a)
        this.b.copy(b)
        this.c.copy(c)

        return this
    }

    /**
     * Sets the triangle's vertices by copying the given array values.
     *
     * @param points An array with 3D points.
     * @param i0 The array index representing the first corner of the triangle.
     * @param i1 The array index representing the second corner of the triangle.
     * @param i2 The array index representing the third corner of the triangle.
     * @return A reference to this triangle.
     */
    public fun setFromPointsAndIndices(points: List<Vector3>, i0: Int, i1: Int, i2: Int): Triangle {
        a.copy(points[i0])
        b.copy(points[i1])
        c.copy(points[i2])

        return this
    }

    /**
     * Sets the triangle's vertices by copying the given attribute values.
     *
     * @param attribute A buffer attribute with 3D points data.
     * @param i0 The attribute index representing the first corner of the triangle.
     * @param i1 The attribute index representing the second corner of the triangle.
     * @param i2 The attribute index representing the third corner of the triangle.
     * @return A reference to this triangle.
     */
    public fun setFromAttributeAndIndices(attribute: AttributeLike, i0: Int, i1: Int, i2: Int): Triangle {
        a.fromBufferAttribute(attribute, i0)
        b.fromBufferAttribute(attribute, i1)
        c.fromBufferAttribute(attribute, i2)

        return this
    }

    /**
     * Returns a new triangle with copied values from this instance.
     *
     * @return A clone of this instance.
     */
    public fun clone(): Triangle = Triangle().copy(this)

    /**
     * Copies the values of the given triangle to this instance.
     *
     * @param triangle The triangle to copy.
     * @return A reference to this triangle.
     */
    public fun copy(triangle: Triangle): Triangle {
        a.copy(triangle.a)
        b.copy(triangle.b)
        c.copy(triangle.c)

        return this
    }

    /**
     * Computes the area of the triangle.
     *
     * @return The triangle's area.
     */
    public fun getArea(): Double {
        val v0 = Vector3()
        val v1 = Vector3()

        v0.subVectors(c, b)
        v1.subVectors(a, b)

        return v0.cross(v1).length() * 0.5
    }

    /**
     * Computes the midpoint of the triangle.
     *
     * @param target The target vector that is used to store the method's result.
     * @return The triangle's midpoint.
     */
    public fun getMidpoint(target: Vector3): Vector3 =
        target.addVectors(a, b).add(c).multiplyScalar(1.0 / 3.0)

    /**
     * Computes the normal of the triangle.
     *
     * @param target The target vector that is used to store the method's result.
     * @return The triangle's normal.
     */
    public fun getNormal(target: Vector3): Vector3 = getNormal(a, b, c, target)

    /**
     * Computes a plane the triangle lies within.
     *
     * @param target The target plane that is used to store the method's result.
     * @return The plane the triangle lies within.
     */
    public fun getPlane(target: Plane): Plane = target.setFromCoplanarPoints(a, b, c)

    /**
     * Computes a barycentric coordinates from the given vector.
     * Returns `null` if the triangle is degenerate.
     *
     * @param point A point in 3D space.
     * @param target The target vector that is used to store the method's result.
     * @return The barycentric coordinates for the given point, or `null` if degenerate.
     */
    public fun getBarycoord(point: Vector3, target: Vector3): Vector3? =
        getBarycoord(point, a, b, c, target)

    /**
     * Computes the value barycentrically interpolated for the given point on the
     * triangle. Returns `null` if the triangle is degenerate.
     *
     * @param point Position of interpolated point.
     * @param v1 Value to interpolate of first vertex.
     * @param v2 Value to interpolate of second vertex.
     * @param v3 Value to interpolate of third vertex.
     * @param target The target vector that is used to store the method's result.
     * @return The interpolated value, or `null` if the triangle is degenerate.
     */
    public fun getInterpolation(point: Vector3, v1: Vector3, v2: Vector3, v3: Vector3, target: Vector3): Vector3? =
        getInterpolation(point, a, b, c, v1, v2, v3, target)

    /**
     * Returns `true` if the given point, when projected onto the plane of the
     * triangle, lies within the triangle.
     *
     * @param point The point in 3D space to test.
     * @return Whether the given point, when projected onto the plane of the
     * triangle, lies within the triangle or not.
     */
    public fun containsPoint(point: Vector3): Boolean = containsPoint(point, a, b, c)

    /**
     * Returns `true` if the triangle is oriented towards the given direction.
     *
     * @param direction The (normalized) direction vector.
     * @return Whether the triangle is oriented towards the given direction or not.
     */
    public fun isFrontFacing(direction: Vector3): Boolean = isFrontFacing(a, b, c, direction)

    /**
     * Returns `true` if this triangle intersects with the given box.
     *
     * @param box The box to intersect.
     * @return Whether this triangle intersects with the given box or not.
     */
    public fun intersectsBox(box: Box3): Boolean = box.intersectsTriangle(this)

    /**
     * Returns the closest point on the triangle to the given point.
     *
     * @param p The point to compute the closest point for.
     * @param target The target vector that is used to store the method's result.
     * @return The closest point on the triangle.
     */
    public fun closestPointToPoint(p: Vector3, target: Vector3): Vector3 {
        val a = this.a
        val b = this.b
        val c = this.c
        var v: Double
        var w: Double

        // algorithm thanks to Real-Time Collision Detection by Christer Ericson,
        // published by Morgan Kaufmann Publishers, (c) 2005 Elsevier Inc.,
        // under the accompanying license; see chapter 5.1.5 for detailed explanation.
        // basically, we're distinguishing which of the voronoi regions of the triangle
        // the point lies in with the minimum amount of redundant computation.
        val vab = Vector3()
        val vac = Vector3()
        val vap = Vector3()
        val vbp = Vector3()
        val vcp = Vector3()
        val vbc = Vector3()

        vab.subVectors(b, a)
        vac.subVectors(c, a)
        vap.subVectors(p, a)
        val d1 = vab.dot(vap)
        val d2 = vac.dot(vap)
        if (d1 <= 0 && d2 <= 0) {
            // vertex region of A; barycentric coords (1, 0, 0)
            return target.copy(a)
        }

        vbp.subVectors(p, b)
        val d3 = vab.dot(vbp)
        val d4 = vac.dot(vbp)
        if (d3 >= 0 && d4 <= d3) {
            // vertex region of B; barycentric coords (0, 1, 0)
            return target.copy(b)
        }

        val vc = d1 * d4 - d3 * d2
        if (vc <= 0 && d1 >= 0 && d3 <= 0) {
            v = d1 / (d1 - d3)
            // edge region of AB; barycentric coords (1-v, v, 0)
            return target.copy(a).addScaledVector(vab, v)
        }

        vcp.subVectors(p, c)
        val d5 = vab.dot(vcp)
        val d6 = vac.dot(vcp)
        if (d6 >= 0 && d5 <= d6) {
            // vertex region of C; barycentric coords (0, 0, 1)
            return target.copy(c)
        }

        val vb = d5 * d2 - d1 * d6
        if (vb <= 0 && d2 >= 0 && d6 <= 0) {
            w = d2 / (d2 - d6)
            // edge region of AC; barycentric coords (1-w, 0, w)
            return target.copy(a).addScaledVector(vac, w)
        }

        val va = d3 * d6 - d5 * d4
        if (va <= 0 && (d4 - d3) >= 0 && (d5 - d6) >= 0) {
            vbc.subVectors(c, b)
            w = (d4 - d3) / ((d4 - d3) + (d5 - d6))
            // edge region of BC; barycentric coords (0, 1-w, w)
            return target.copy(b).addScaledVector(vbc, w) // edge region of BC
        }

        // face region
        val denom = 1.0 / (va + vb + vc)
        // u = va * denom
        v = vb * denom
        w = vc * denom

        return target.copy(a).addScaledVector(vab, v).addScaledVector(vac, w)
    }

    /**
     * Returns `true` if this triangle is equal with the given one.
     *
     * Note: unlike the structural [equals] override, this mirrors three.js's
     * `Triangle.equals(triangle)` (corner-by-corner value comparison, non-null arg).
     *
     * @param triangle The triangle to test for equality.
     * @return Whether this triangle is equal with the given one.
     */
    override fun equals(other: Any?): Boolean =
        other is Triangle && other.a == a && other.b == b && other.c == c

    override fun hashCode(): Int {
        var result = a.hashCode()
        result = 31 * result + b.hashCode()
        result = 31 * result + c.hashCode()
        return result
    }

    override fun toString(): String = "Triangle(a=$a, b=$b, c=$c)"
}
